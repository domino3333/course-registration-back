import http from "k6/http";

const BASE_URL = "http://localhost:8080";

export function setup() {
    for (let i = 1; i <= 10; i++) {
        const payload = JSON.stringify({
            email: `user${i}@test.com`,
            password: "123123",
            name: `테스트유저${i}`,
            code: `2026${String(i).padStart(4, "0")}`,
            gender: "man"
        });

        const res = http.post(`${BASE_URL}/api/auth/signUp`, payload, {
            headers: {
                "Content-Type": "application/json"
            }
        });

        console.log(`user${i}@test.com 회원가입 status: ${res.status}`);
    }
}

export default function () {
    // 일단 비워둠
}