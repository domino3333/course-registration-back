import http from "k6/http";
import { check, sleep } from "k6";
import { Counter, Trend } from "k6/metrics";

const BASE_URL = __ENV.BASE_URL || "http://localhost:8080";

const USER_START_NO = Number(__ENV.USER_START_NO || 1);
const USER_COUNT = Number(__ENV.USER_COUNT || 5000);
const ARRIVAL_WINDOW_SECONDS = Number(__ENV.ARRIVAL_WINDOW_SECONDS || 60);
const DEFAULT_POLL_MILLIS = Number(__ENV.DEFAULT_POLL_MILLIS || 10000);
const MIN_POLL_MILLIS = Number(__ENV.MIN_POLL_MILLIS || 1000);
const STAY_SECONDS = Number(__ENV.STAY_SECONDS || 10);
const MAX_WAIT_FOR_ADMIT_SECONDS = Number(__ENV.MAX_WAIT_FOR_ADMIT_SECONDS || 21600);

export const loginSuccessTotal = new Counter("login_success_total");
export const loginFailedTotal = new Counter("login_failed_total");
export const enterQueueSuccessTotal = new Counter("enter_queue_success_total");
export const enterQueueFailedTotal = new Counter("enter_queue_failed_total");
export const admitTrueTotal = new Counter("admit_true_total");
export const admitFalseTotal = new Counter("admit_false_total");
export const admitTimeoutTotal = new Counter("admit_timeout_total");
export const mainVisitSuccessTotal = new Counter("main_visit_success_total");
export const leaveSuccessTotal = new Counter("leave_success_total");
export const queuePollTotal = new Counter("queue_poll_total");
export const adaptivePollMillis = new Trend("adaptive_poll_millis");
export const queueWaitSeconds = new Trend("queue_wait_seconds");

export const options = {
    scenarios: {
        queue_5000_adaptive_polling: {
            executor: "per-vu-iterations",
            vus: USER_COUNT,
            iterations: 1,
            maxDuration: "6h",
        },
    },
};

function randomSleepSeconds(min, max) {
    return Math.floor(Math.random() * (max - min + 1)) + min;
}

function authHeaders(token) {
    return {
        headers: {
            Authorization: `Bearer ${token}`,
        },
    };
}

function jsonOrNull(res) {
    try {
        return res.json();
    } catch (e) {
        return null;
    }
}

function shouldSampleLog(userNo) {
    return userNo <= USER_START_NO + 4 || userNo % 500 === 0;
}

function getNextPollMillis(status) {
    if (!status || typeof status.nextPollMillis !== "number") {
        return DEFAULT_POLL_MILLIS;
    }

    if (status.allowed) {
        return MIN_POLL_MILLIS;
    }

    return Math.max(status.nextPollMillis, MIN_POLL_MILLIS);
}

export default function () {
    const userNo = USER_START_NO + __VU - 1;
    const email = `user${userNo}@test.com`;

    const waitBeforeLogin = randomSleepSeconds(0, ARRIVAL_WINDOW_SECONDS);
    sleep(waitBeforeLogin);

    const loginRes = http.post(
        `${BASE_URL}/api/auth/login`,
        JSON.stringify({
            email,
            password: "123123",
        }),
        {
            headers: {
                "Content-Type": "application/json",
            },
            timeout: "30s",
        }
    );

    const loginOk = check(loginRes, {
        "login status is 200": (res) => res.status === 200,
        "login accessToken exists": (res) => !!res.json("accessToken"),
    });

    if (!loginOk) {
        loginFailedTotal.add(1);
        console.log(`${email} login failed status=${loginRes.status}`);
        return;
    }

    loginSuccessTotal.add(1);

    const token = loginRes.json("accessToken");
    const auth = authHeaders(token);

    const enterRes = http.post(`${BASE_URL}/api/queue/enter`, null, auth);
    const enterOk = check(enterRes, {
        "enter queue status is 200": (res) => res.status === 200,
    });

    if (!enterOk) {
        enterQueueFailedTotal.add(1);
        console.log(`${email} enter queue failed status=${enterRes.status}`);
        return;
    }

    enterQueueSuccessTotal.add(1);

    const enteredAt = Date.now();
    let admitted = false;

    while (!admitted) {
        const waitedSeconds = (Date.now() - enteredAt) / 1000;

        if (waitedSeconds > MAX_WAIT_FOR_ADMIT_SECONDS) {
            admitTimeoutTotal.add(1);
            http.del(`${BASE_URL}/api/queue/leave`, null, auth);
            console.log(`${email} admit timeout after ${Math.round(waitedSeconds)}s`);
            return;
        }

        const statusRes = http.get(`${BASE_URL}/api/queue/status`, auth);
        queuePollTotal.add(1);

        const statusOk = check(statusRes, {
            "queue status is 200": (res) => res.status === 200,
        });

        if (!statusOk) {
            adaptivePollMillis.add(DEFAULT_POLL_MILLIS);
            sleep(DEFAULT_POLL_MILLIS / 1000);
            continue;
        }

        const status = jsonOrNull(statusRes);
        const nextPollMillis = getNextPollMillis(status);
        adaptivePollMillis.add(nextPollMillis);

        if (status && status.allowed) {
            const admitRes = http.post(`${BASE_URL}/api/queue/admit`, null, auth);

            check(admitRes, {
                "admit status is 200": (res) => res.status === 200,
            });

            if (admitRes.body === "true") {
                admitted = true;
                admitTrueTotal.add(1);
                queueWaitSeconds.add(waitedSeconds);

                if (shouldSampleLog(userNo)) {
                    console.log(`${email} admitted after ${Math.round(waitedSeconds)}s`);
                }
                break;
            }

            admitFalseTotal.add(1);
        }

        sleep(nextPollMillis / 1000);
    }

    const ticketRes = http.get(`${BASE_URL}/api/queue/ticket`, auth);
    const meRes = http.get(`${BASE_URL}/api/auth`, auth);
    const lectureRes = http.get(`${BASE_URL}/api/lecture`, auth);
    const registrationRes = http.get(`${BASE_URL}/api/registration`, auth);

    const mainOk = check(null, {
        "main ticket is true": () => ticketRes.status === 200 && ticketRes.body === "true",
        "main member info is 200": () => meRes.status === 200,
        "main lecture list is 200": () => lectureRes.status === 200,
        "main registration list is 200": () => registrationRes.status === 200,
    });

    if (mainOk) {
        mainVisitSuccessTotal.add(1);
    }

    sleep(STAY_SECONDS);

    const leaveRes = http.del(`${BASE_URL}/api/queue/leave`, null, auth);
    const leaveOk = check(leaveRes, {
        "leave status is 200": (res) => res.status === 200,
    });

    if (leaveOk) {
        leaveSuccessTotal.add(1);
    }

    if (shouldSampleLog(userNo)) {
        console.log(`${email} left after staying ${STAY_SECONDS}s`);
    }
}
