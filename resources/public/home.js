const $ = x => document.querySelector(x);

document.addEventListener('keydown', (event) => {
    if (event.key === 'n') {
        $('#new-slideshow').click();
    }
});

document.querySelectorAll("img[data-src]").forEach(img => {
    img.parentElement.addEventListener("mouseenter", () => {
        if (!img.src) {
            img.src = img.dataset.src;
        }
    }, { once: true });
});
