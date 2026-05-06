import http from "k6/http";
import { check, sleep } from "k6";

const BASE_URL = "http://localhost:8080";

export const options = {
    scenarios: {
        random_queue_users: {
            executor: "per-vu-iterations",
            vus: 200,
            iterations: 1,
            maxDuration: "20m",
        },
    },
};

function randomSleepSeconds(min, max) {
    return Math.floor(Math.random() * (max - min + 1)) + min;
}

export default function () {
    const userNo = __VU;
    const email = `user${userNo}@test.com`;

    const waitBeforeEnter = randomSleepSeconds(0, 300);
    console.log(`${email} will enter after ${waitBeforeEnter}s`);
    sleep(waitBeforeEnter);

    const loginPayload = JSON.stringify({
        email,
        password: "123123",
    });

    const loginRes = http.post(`${BASE_URL}/api/auth/login`, loginPayload, {
        headers: {
            "Content-Type": "application/json",
        },
    });

    check(loginRes, {
        "login success": (res) => res.status === 200,
        "token exists": (res) => !!res.json("accessToken"),
    });

    const token = loginRes.json("accessToken");

    const authHeaders = {
        headers: {
            Authorization: `Bearer ${token}`,
        },
    };

    const enterRes = http.post(`${BASE_URL}/api/queue/enter`, null, authHeaders);

    check(enterRes, {
        "enter queue success": (res) => res.status === 200,
    });

    let admitted = false;

    while (!admitted) {
        const statusRes = http.get(`${BASE_URL}/api/queue/status`, authHeaders);

        check(statusRes, {
            "status success": (res) => res.status === 200,
        });

        const status = statusRes.json();

        console.log(
            `${email} rank=${status.rank}, waitingAhead=${status.waitingAhead}, allowed=${status.allowed}`
        );

        if (status.allowed) {
            const admitRes = http.post(`${BASE_URL}/api/queue/admit`, null, authHeaders);

            check(admitRes, {
                "admit request success": (res) => res.status === 200,
            });

            if (admitRes.body === "true") {
                admitted = true;
                console.log(`${email} admitted`);
                break;
            }
        }

        sleep(2);
    }

    sleep(10);

    const leaveRes = http.del(`${BASE_URL}/api/queue/leave`, null, authHeaders);

    check(leaveRes, {
        "leave success": (res) => res.status === 200,
    });

    console.log(`${email} left`);
}
