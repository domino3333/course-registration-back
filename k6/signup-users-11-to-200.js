import http from "k6/http";
import { check } from "k6";

const BASE_URL = "http://localhost:8080";

export const options = {
    vus: 1,
    iterations: 1,
};

export function setup() {
    for (let i = 11; i <= 200; i++) {
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
        });

        check(res, {
            "signUp succeeded": (r) => r.status === 200 || r.status === 201,
        });

        console.log(`${email} 회원가입 status: ${res.status}`);
    }
}

export default function () {
    // setup()에서 테스트 회원만 생성한다.
}
