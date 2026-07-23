function persist(id, k) {
    const textarea = document.getElementById(id);
    if (!textarea) return;

    // Restore previous contents
    const saved = sessionStorage.getItem(k);
    if (saved !== null) {
        textarea.value = saved;
    }

    // Save on every key event
    textarea.addEventListener("keyup", () => {
        sessionStorage.setItem(k, textarea.value);
    });
}

persist('questions', 'q')
persist('answers', 'a')
