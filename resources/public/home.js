const $ = x => document.querySelector(x);

document.addEventListener('keydown', (event) => {
    if (event.key === 'n') {
        $('#new-slideshow').click();
    }
});

function activateImages() {
    document.querySelectorAll("img[data-src]").forEach(img => {
        img.parentElement.addEventListener("mouseenter", () => {
            if (!img.src) {
                img.src = img.dataset.src;
            }
        }, {once: true});
    });
}
activateImages();

async function getTextFromClipboard() {
    try {
        // 1. Read clipboard items (requires clipboard-read permission)
        const items = await navigator.clipboard.read();

        for (const item of items) {
            // 2. Check if the item contains HTML format
            if (item.types.includes('text/plain')) {
                const blob = await item.getType('text/plain');
                return blob.text();
            }
        }

        console.warn('No HTML format found on the clipboard.');
        return null;
    } catch (err) {
        console.error('Failed to read clipboard:', err);
    }
}

async function postTextToAPI() {
    const content = await getTextFromClipboard();

    if (!content) {
        console.warn('No HTML to send.');
        return;
    }

    try {
        const response = await fetch('/api/this-week', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ content })
        });

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
    } catch (err) {
        console.error('Failed to post HTML to API:', err);
    }
}
