const API_BASE_URL = "http://localhost:8080";
let authToken = localStorage.getItem("jwtToken") || null;
let currentTenantId = localStorage.getItem("currentTenantId") || null;
let currentChannelId = localStorage.getItem("currentChannelId") || null;
let stompClient = null;

document.addEventListener("DOMContentLoaded", () => {
    if (authToken) {
        showAppScreen();
    } else {
        showAuthScreen();
    }
    setupAuthListeners();
    setupChatListeners();
});

// Switch between Login and Signup tabs smoothly
function switchAuthTab(tab) {
    const loginForm = document.getElementById("login-form");
    const signupForm = document.getElementById("signup-form");
    const tabLogin = document.getElementById("tab-login");
    const tabSignup = document.getElementById("tab-signup");
    const subtitle = document.getElementById("auth-subtitle");
    document.getElementById("auth-error").textContent = "";

    if (tab === 'login') {
        loginForm.style.display = "block";
        signupForm.style.display = "none";
        tabLogin.classList.add("active");
        tabSignup.classList.remove("active");
        subtitle.textContent = "Sign in to your workspace";
    } else {
        loginForm.style.display = "none";
        signupForm.style.display = "block";
        tabSignup.classList.add("active");
        tabLogin.classList.remove("active");
        subtitle.textContent = "Create an account to join WalkyTalky";
    }
}

function setupAuthListeners() {
    // Handle Login submission
    document.getElementById("login-form").addEventListener("submit", async (e) => {
        e.preventDefault();
        const email = document.getElementById("login-email").value.trim();
        const password = document.getElementById("login-password").value.trim();
        const errorEl = document.getElementById("auth-error");

        try {
            const res = await fetch(`${API_BASE_URL}/api/userauth/login`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ email, password })
            });

            if (!res.ok) {
                const errText = await res.text();
                throw new Error(errText || "Invalid email or password");
            }

            const data = await res.json();
            authToken = data.jwtToken;
            localStorage.setItem("jwtToken", authToken);
            localStorage.setItem("userId", data.userId);
            localStorage.setItem("userEmail", data.email || email);

            showAppScreen();
        } catch (err) {
            errorEl.textContent = err.message;
        }
    });

    // Handle Signup registration submission
    document.getElementById("signup-form").addEventListener("submit", async (e) => {
        e.preventDefault();
        const fullName = document.getElementById("signup-fullname").value.trim();
        const email = document.getElementById("signup-email").value.trim();
        const password = document.getElementById("signup-password").value.trim();
        const errorEl = document.getElementById("auth-error");

        try {
            const res = await fetch(`${API_BASE_URL}/api/userauth/signup`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ fullName, email, password })
            });

            if (!res.ok) {
                const errText = await res.text();
                throw new Error(errText || "Registration failed. Email might already be taken.");
            }

            alert("Account created successfully! Please sign in.");
            switchAuthTab('login');
            document.getElementById("login-email").value = email;
        } catch (err) {
            errorEl.textContent = err.message;
        }
    });
}

function showAuthScreen() {
    document.getElementById("auth-container").style.display = "flex";
    document.getElementById("app-container").style.display = "none";
}

async function showAppScreen() {
    document.getElementById("auth-container").style.display = "none";
    document.getElementById("app-container").style.display = "flex";
    
    document.getElementById("user-display-name").textContent = localStorage.getItem("userEmail") || "Online User";

    await initializeWorkspace();
}

async function initializeWorkspace() {
    try {
        currentTenantId = 1;
        localStorage.setItem("currentTenantId", currentTenantId);

        await loadChannels(currentTenantId);
        await loadMembers(currentTenantId);
    } catch (err) {
        console.error("Workspace initialization error:", err);
    }
}

async function loadChannels(tenantId) {
    try {
        const res = await fetch(`${API_BASE_URL}/api/tenants/${tenantId}/channels`, {
            headers: { "Authorization": `Bearer ${authToken}` }
        });
        if (!res.ok) throw new Error("Failed to load channels");

        const channels = await res.json();
        const listEl = document.getElementById("channel-list");
        listEl.innerHTML = "";

        if (channels.length === 0) {
            listEl.innerHTML = "<li style='color:#64748b; font-size:0.85rem;'>No channels available</li>";
            return;
        }

        // Check if current user is admin/owner to allow deletion UI
        const currentEmail = localStorage.getItem("userEmail");
        const membersRes = await fetch(`${API_BASE_URL}/api/tenants/${tenantId}/members`, {
            headers: { "Authorization": `Bearer ${authToken}` }
        });
        let isAdmin = false;
        if (membersRes.ok) {
            const members = await membersRes.json();
            const me = members.find(m => m.email === currentEmail);
            if (me && (me.role === "ADMIN" || me.role === "OWNER")) {
                isAdmin = true;
            }
        }

        channels.forEach((ch, index) => {
            const li = document.createElement("li");
            li.style.display = "flex";
            li.style.justifyContent = "space-between";
            li.style.alignItems = "center";

            const nameSpan = document.createElement("span");
            nameSpan.textContent = `# ${ch.name}`;
            nameSpan.style.cursor = "pointer";
            nameSpan.style.flex = "1";
            nameSpan.addEventListener("click", () => {
                document.querySelectorAll("#channel-list li").forEach(i => i.classList.remove("active"));
                li.classList.add("active");
                selectChannel(tenantId, ch.channelId, ch.name, ch.description);
            });
            li.appendChild(nameSpan);

            // Add delete button ONLY for Admins/Owners
            if (isAdmin) {
                const deleteBtn = document.createElement("button");
                deleteBtn.textContent = "🗑️";
                deleteBtn.title = "Delete Channel";
                deleteBtn.style.cssText = "background: none; border: none; cursor: pointer; font-size: 0.8rem; padding: 2px 5px; opacity: 0.6;";
                deleteBtn.onmouseover = () => deleteBtn.style.opacity = "1";
                deleteBtn.onmouseout = () => deleteBtn.style.opacity = "0.6";

                deleteBtn.addEventListener("click", async (e) => {
                    e.stopPropagation(); // Prevent channel selection when clicking delete
                    if (confirm(`Are you sure you want to delete #${ch.name}? This action cannot be undone.`)) {
                        try {
                            const delRes = await fetch(`${API_BASE_URL}/api/tenants/${tenantId}/channels/${ch.channelId}`, {
                                method: "DELETE",
                                headers: { "Authorization": `Bearer ${authToken}` }
                            });

                            if (!delRes.ok) {
                                const errText = await delRes.text();
                                throw new Error(errText || "Failed to delete channel");
                            }

                            // Reload channel list after successful deletion
                            loadChannels(tenantId);
                        } catch (err) {
                            alert("Error deleting channel: " + err.message);
                        }
                    }
                });
                li.appendChild(deleteBtn);
            }

            if (index === 0 && !currentChannelId) {
                selectChannel(tenantId, ch.channelId, ch.name, ch.description);
                li.classList.add("active");
            } else if (String(ch.channelId) === String(currentChannelId)) {
                li.classList.add("active");
                selectChannel(tenantId, ch.channelId, ch.name, ch.description, false);
            }

            listEl.appendChild(li);
        });
    } catch (err) {
        console.error(err);
    }
}

async function loadMembers(tenantId) {
    try {
        const res = await fetch(`${API_BASE_URL}/api/tenants/${tenantId}/members`, {
            headers: { "Authorization": `Bearer ${authToken}` }
        });
        if (!res.ok) return;

        const members = await res.json();
        const memberListEl = document.getElementById("member-list");
        memberListEl.innerHTML = "";

        const currentEmail = localStorage.getItem("userEmail");
        let currentUserRole = "MEMBER";

        members.forEach(m => {
            const li = document.createElement("li");
            const isMe = m.email === currentEmail;
            
            if (isMe) {
                currentUserRole = m.role;
            }

            li.innerHTML = `🟢 ${escapeHtml(m.fullName)} <span style="font-size:0.75rem; color:#64748b;">(${m.role})</span>`;
            memberListEl.appendChild(li);
        });

        // Dynamic Role-Based UI Toggle for Admin Panel
        const adminPanel = document.getElementById("admin-panel-section");
        if (adminPanel) {
            if (currentUserRole === "ADMIN" || currentUserRole === "OWNER") {
                adminPanel.style.display = "block";
                setupAdminListeners();
            } else {
                adminPanel.style.display = "none";
            }
        }
    } catch (err) {
        console.error("Failed to load members", err);
    }
}

function setupAdminListeners() {
    const createBtn = document.getElementById("create-channel-btn");
    if (createBtn && !createBtn.dataset.listenerAttached) {
        createBtn.dataset.listenerAttached = "true";
        createBtn.addEventListener("click", async () => {
            const nameInput = document.getElementById("new-channel-name");
            const descInput = document.getElementById("new-channel-desc");
            
            const name = nameInput.value.trim();
            const description = descInput.value.trim();

            if (!name) {
                alert("Please enter a channel name.");
                return;
            }

            try {
                const res = await fetch(`${API_BASE_URL}/api/tenants/${currentTenantId}/channels`, {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json",
                        "Authorization": `Bearer ${authToken}`
                    },
                    body: JSON.stringify({ name, description, isPrivate: false })
                });

                if (!res.ok) {
                    const errText = await res.text();
                    throw new Error(errText || "Failed to create channel");
                }

                nameInput.value = "";
                descInput.value = "";
                loadChannels(currentTenantId);
            } catch (err) {
                alert("Error: " + err.message);
            }
        });
    }

    const inviteBtn = document.getElementById("admin-invite-btn");
    if (inviteBtn && !inviteBtn.dataset.listenerAttached) {
        inviteBtn.dataset.listenerAttached = "true";
        inviteBtn.addEventListener("click", () => {
            navigator.clipboard.writeText(window.location.origin + window.location.pathname);
            alert("Workspace link copied to clipboard!");
        });
    }
}

function selectChannel(tenantId, channelId, name, description, reloadMsgs = true) {
    currentChannelId = channelId;
    localStorage.setItem("currentChannelId", channelId);
    document.getElementById("current-channel-title").textContent = `# ${name}`;
    document.getElementById("current-channel-desc").textContent = description || "Workspace communication channel";
    document.getElementById("message-input").placeholder = `Message #${name}...`;

    if (reloadMsgs) {
        loadMessages(tenantId, channelId);
    }
    connectWebSocket(tenantId, channelId);
}

async function loadMessages(tenantId, channelId) {
    try {
        const res = await fetch(`${API_BASE_URL}/api/tenants/${tenantId}/channels/${channelId}/messages`, {
            headers: { "Authorization": `Bearer ${authToken}` }
        });
        if (!res.ok) throw new Error("Failed to load messages");

        const pageData = await res.json();
        const container = document.getElementById("message-container");
        container.innerHTML = "";

        const messages = pageData.content ? pageData.content.reverse() : [];
        if (messages.length === 0) {
            container.innerHTML = `<div style="text-align:center; color:#64748b; margin-top:40px;">No messages yet. Start the conversation!</div>`;
        } else {
            messages.forEach(msg => appendMessageToDOM(msg));
            container.scrollTop = container.scrollHeight;
        }
    } catch (err) {
        console.error(err);
    }
}

function setupChatListeners() {
    const messageForm = document.getElementById("message-form");
    const messageInput = document.getElementById("message-input");

    messageForm.addEventListener("submit", (e) => {
        e.preventDefault();
        const content = messageInput.value.trim();
        if (!content || !currentChannelId) return;

        if (stompClient && stompClient.connected) {
            stompClient.send(
                `/app/tenants/${currentTenantId}/channels/${currentChannelId}/send`,
                { "Authorization": `Bearer ${authToken}` },
                JSON.stringify({ content })
            );
        } else {
            fetch(`${API_BASE_URL}/api/tenants/${currentTenantId}/channels/${currentChannelId}/messages`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    "Authorization": `Bearer ${authToken}`
                },
                body: JSON.stringify({ content })
            });
        }
        messageInput.value = "";
    });
}

function connectWebSocket(tenantId, channelId) {
    if (stompClient && stompClient.connected) {
        stompClient.disconnect(() => {
            initStomp(tenantId, channelId);
        });
    } else {
        initStomp(tenantId, channelId);
    }
}

function initStomp(tenantId, channelId) {
    const socket = new SockJS(`${API_BASE_URL}/ws`);
    stompClient = Stomp.over(socket);
    stompClient.debug = null;

    stompClient.connect({ "Authorization": `Bearer ${authToken}` }, () => {
        stompClient.subscribe(`/topic/tenants/${tenantId}/channels/${channelId}`, (message) => {
            const incomingMsg = JSON.parse(message.body);
            const container = document.getElementById("message-container");
            
            if (container.querySelector("div[style*='text-align:center']")) {
                container.innerHTML = "";
            }

            appendMessageToDOM(incomingMsg);
            container.scrollTop = container.scrollHeight;
        });
    }, (error) => {
        console.error("STOMP connection error:", error);
    });
}

function appendMessageToDOM(msg) {
    const container = document.getElementById("message-container");
    const div = document.createElement("div");
    div.classList.add("message");
    div.dataset.messageId = msg.messageId;

    const currentUserId = Number(localStorage.getItem("userId")); // ensure numeric comparison
    const isMyMessage = msg.senderId === currentUserId;

    // Check if user is Admin/Owner based on member list
    const currentUserRole = window.currentUserRole || "MEMBER";
    const isAdminOrOwner = currentUserRole === "ADMIN" || currentUserRole === "OWNER";

    const timeFormatted = new Date(msg.createdAt || Date.now()).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    const editedTag = msg.isEdited ? `<span style="font-size:0.7rem; color:#94a3b8; margin-left:4px;">(edited)</span>` : "";

    div.innerHTML = `
        <div class="message-header-row" style="display:flex; justify-content:space-between; align-items:center;">
            <div>
                <span class="sender">${escapeHtml(msg.senderFullName || 'User')}</span>
                <span class="time">${timeFormatted}</span>
                ${editedTag}
            </div>
            <div class="message-actions" style="display:flex; gap:6px;">
                ${isMyMessage ? `<button class="edit-msg-btn" title="Edit Message" style="background:none; border:none; cursor:pointer; font-size:0.8rem; opacity:0.7;">✏️</button>` : ''}
                ${(isMyMessage || isAdminOrOwner) ? `<button class="delete-msg-btn" title="Delete Message" style="background:none; border:none; cursor:pointer; font-size:0.8rem; opacity:0.7;">🗑️</button>` : ''}
            </div>
        </div>
        <div class="text" id="msg-text-${msg.messageId}">${escapeHtml(msg.content)}</div>
    `;

    // Edit Event Handler (Only for Message Author)
    const editBtn = div.querySelector(".edit-msg-btn");
    if (editBtn) {
        editBtn.addEventListener("click", () => handleEditMessage(msg));
    }

    // Delete Event Handler (For Author or Admin/Owner)
    const deleteBtn = div.querySelector(".delete-msg-btn");
    if (deleteBtn) {
        deleteBtn.addEventListener("click", () => handleDeleteMessage(msg.messageId));
    }

    container.appendChild(div);
}

// Handler for editing a message
async function handleEditMessage(msg) {
    const newContent = prompt("Edit your message:", msg.content);
    if (!newContent || newContent.trim() === "" || newContent === msg.content) return;

    try {
        const res = await fetch(`${API_BASE_URL}/api/tenants/${currentTenantId}/channels/${currentChannelId}/messages/${msg.messageId}`, {
            method: "PUT",
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${authToken}`
            },
            body: JSON.stringify({ content: newContent.trim() })
        });

        if (!res.ok) throw new Error("Failed to edit message");

        // Reload messages to display the updated text and (edited) flag
        loadMessages(currentTenantId, currentChannelId);
    } catch (err) {
        alert("Error editing message: " + err.message);
    }
}

// Handler for deleting a message
async function handleDeleteMessage(messageId) {
    if (!confirm("Are you sure you want to delete this message?")) return;

    try {
        const res = await fetch(`${API_BASE_URL}/api/tenants/${currentTenantId}/channels/${currentChannelId}/messages/${messageId}`, {
            method: "DELETE",
            headers: { "Authorization": `Bearer ${authToken}` }
        });

        if (!res.ok) throw new Error("Failed to delete message");

        // Remove the element directly from DOM or reload list
        loadMessages(currentTenantId, currentChannelId);
    } catch (err) {
        alert("Error deleting message: " + err.message);
    }
}

function logout() {
    localStorage.clear();
    location.reload();
}

function escapeHtml(str) {
    if (!str) return "";
    return str.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;");
}