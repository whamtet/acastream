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
            "width=500,height=600,toolbar=no,location=no,status=no,menubar=no,scrollbars=yes,resizable=yes"
        );
    }
});
