import http from "k6/http";
import { check, sleep } from "k6";

const BASE_URL = "http://localhost:8080";

export const options = {
    vus: 1,
    iterations: 1,
};

export default function () {
    const email = "user1@test.com";

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
        "login status is 200": (res) => res.status === 200,
        "accessToken exists": (res) => !!res.json("accessToken"),
    });

    const token = loginRes.json("accessToken");

    const authHeaders = {
        headers: {
            Authorization: `Bearer ${token}`,
        },
    };

    const enterRes = http.post(`${BASE_URL}/api/queue/enter`, null, authHeaders);

    check(enterRes, {
        "enter queue status is 200": (res) => res.status === 200,
    });

    const statusRes = http.get(`${BASE_URL}/api/queue/status`, authHeaders);

    check(statusRes, {
        "queue status is 200": (res) => res.status === 200,
    });

    console.log(`queue status body: ${statusRes.body}`);

    const status = statusRes.json();

    if (status.allowed) {
        const admitRes = http.post(`${BASE_URL}/api/queue/admit`, null, authHeaders);

        check(admitRes, {
            "admit status is 200": (res) => res.status === 200,
            "admit result is true": (res) => res.body === "true",
        });
    }

    const ticketRes = http.get(`${BASE_URL}/api/queue/ticket`, authHeaders);

    check(ticketRes, {
        "ticket status is 200": (res) => res.status === 200,
    });

    console.log(`ticket body: ${ticketRes.body}`);

    sleep(1);
}