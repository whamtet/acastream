const $ = x => document.querySelector(x);

const clipboardListener_ = (suffix) => document.getElementById("pasteClipboard" + suffix).addEventListener("click", async () => {
    try {
        const items = await navigator.clipboard.read();

        for (const item of items) {
            for (const type of item.types) {
                if (type.startsWith("image/")) {
                    const blob = await item.getType(type);
                    console.log("Blob size (bytes):", blob.size);

                    const file = new File([blob], "clipboard.png", {
                        type: blob.type
                    });

                    const dt = new DataTransfer();
                    dt.items.add(file);

                    const input = document.getElementById("clipboard" + suffix);
                    input.files = dt.files;

                    console.log("Clipboard image loaded into file input");

                    document.getElementById("clipboardSubmit" + suffix).click();
                    return;
                }
            }
        }

        console.log("No image found in clipboard");
    } catch (err) {
        console.error("Failed to read clipboard:", err);
    }
});
const clipboardListener = () => {
    clipboardListener_('');
    clipboardListener_('2');
}

clipboardListener();

document.addEventListener('keydown', (event) => {
    if (event.key === 'Escape') {
        $('#modal').classList.add('hidden');
    }
});
