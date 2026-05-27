document.addEventListener('keydown', (event) => {
    if (event.key === 'r' || event.key === 'R') {
        document.querySelector('#randomLink').click();
    }
    if (event.key === 'e' || event.key === 'E') {
        document.querySelector('#editLink').click();
    }
    if (event.key === 'l' || event.key === 'L') {
        window.open(
            "/key.html",
            "Legend",
            "width=300,height=580,toolbar=no,location=no,status=no,menubar=no,scrollbars=yes,resizable=yes"
        );
    }
    if (event.key === 'q' || event.key === 'Q') {
        const slideshowId = location.href.split('/')[4];
        window.open(
            "/api/qr/" + slideshowId,
            "QR",
            "width=256,height=256,toolbar=no,location=no,status=no,menubar=no,scrollbars=yes,resizable=yes"
        );
    }
});
