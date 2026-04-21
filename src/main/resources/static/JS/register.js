// Entry point
document.addEventListener("DOMContentLoaded", init);

function init() {
    const form = document.querySelector("form");
    form.addEventListener("submit", handleRegister);
}

// Main handler
async function handleRegister(event) {
    event.preventDefault();

    const payload = getFormData();

    if (!validatePayload(payload)) return;

    console.log("Payload:", payload);

    try {
        showLoading();

        const response = await registerUser(payload);

        console.log("Response:", response);

        onRegisterSuccess(payload.email);

    } catch (error) {
        handleError(error);
    }
}

// Extract form data
function getFormData() {
    return {
        username: document.getElementById("username").value.trim(),
        fullName: document.getElementById("fullName").value.trim(),
        email: document.getElementById("email").value.trim(),
        password: document.getElementById("password").value.trim(),
        course: document.getElementById("course").value,
        branch: document.getElementById("branch").value,
        yearOfPassout: parseInt(document.getElementById("yearOfPassout").value)
    };
}

// Validation logic
function validatePayload(data) {
    if (!data.username || !data.email || !data.password) {
        alert("❌ Required fields missing");
        return false;
    }

    if (isNaN(data.yearOfPassout)) {
        alert("❌ Invalid year");
        return false;
    }

    return true;
}

// API call
async function registerUser(payload) {
    const res = await fetch("https://iet-scroll.onrender.com/api/v1/user/register", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(payload)
    });

    if (!res.ok) {
        const errText = await res.text();
        throw new Error(errText);
    }

    return await res.json();
}

// Success handler
function onRegisterSuccess(email) {
    alert("Registered! OTP sent. Check Inbox/Spam");

    localStorage.setItem("userEmail", email);

    // window.location.href = "/otp.html";
}

// Error handler
function handleError(error) {
    console.error(error);
    alert("❌ " + error.message);
}

// UX helper
function showLoading() {
    alert("⏳ Processing... (server may take time)");
}