import http from "k6/http";
import { check, sleep } from "k6";
import { Counter } from "k6/metrics";

const BASE_URL = "http://localhost:8080";
const USER_COUNT = 200;
export const admitTrueTotal = new Counter("admit_true_total");
export const admitFalseTotal = new Counter("admit_false_total");

export const options = {
    scenarios: {
        admit_race: {
            executor: "per-vu-iterations",
            vus: USER_COUNT,
            iterations: 1,
            maxDuration: "1m",
        },
    },
};

export function setup() {
    const users = [];

    for (let i = 1; i <= USER_COUNT; i++) {
        const email = `user${i}@test.com`;

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
            }
        );

        check(loginRes, {
            [`${email} login 200`]: (res) => res.status === 200,
            [`${email} token exists`]: (res) => !!res.json("accessToken"),
        });

        const token = loginRes.json("accessToken");

        const authHeaders = {
            headers: {
                Authorization: `Bearer ${token}`,
            },
        };

        const enterRes = http.post(`${BASE_URL}/api/queue/enter`, null, authHeaders);

        check(enterRes, {
            [`${email} enter queue 200`]: (res) => res.status === 200,
        });

        users.push({
            email,
            token,
        });
    }

    return {
        users,
        startAt: Date.now() + 3000,
    };
}

export default function (data) {
    const user = data.users[__VU - 1];

    const waitMs = data.startAt - Date.now();
    if (waitMs > 0) {
        sleep(waitMs / 1000);
    }

    const authHeaders = {
        headers: {
            Authorization: `Bearer ${user.token}`,
        },
    };

    const admitRes = http.post(`${BASE_URL}/api/queue/admit`, null, authHeaders);

    check(admitRes, {
        "admit status 200": (res) => res.status === 200,
    });

    const admitted = admitRes.body === "true";

    if (admitted) {
        admitTrueTotal.add(1);
    } else {
        admitFalseTotal.add(1);
    }

    //console.log(`${user.email} admit=${admitted}`);
}
