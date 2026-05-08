import http from "k6/http";
import { check, sleep } from "k6";

const BASE_URL = "http://localhost:8080";

const START_USER_NO = 201;
const END_USER_NO = 10000;

export const options = {
    vus: 1,
    iterations: 1,
    setupTimeout: "30m",
};

export function setup() {
    let success = 0;
    let failed = 0;

    for (let i = START_USER_NO; i <= END_USER_NO; i++) {
        const email = `user${i}@test.com`;

        const payload = JSON.stringify({
            email,
            password: "123123",
            name: `테스트유저${i}`,
            code: `2026${String(i).padStart(4, "0")}`,
            gender: "man",
        });

        const res = http.post(`${BASE_URL}/api/auth/signUp`, payload, {
            headers: {
                "Content-Type": "application/json",
            },
            timeout: "30s",
        });

        const ok = res.status === 200 || res.status === 201;

        check(res, {
            "signUp succeeded": () => ok,
        });

        if (ok) {
            success++;
        } else {
            failed++;
            console.log(`${email} 회원가입 실패 status=${res.status}, body=${res.body}`);
        }

        if (i % 100 === 0) {
            console.log(`progress: user${i}, success=${success}, failed=${failed}`);
        }

        sleep(0.02);
    }

    console.log(`회원가입 완료: success=${success}, failed=${failed}`);
}

export default function () {
    // setup()에서 테스트 회원만 생성한다.
}
