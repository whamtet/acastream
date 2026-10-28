const $ = x => document.querySelector(x);

document.addEventListener('keydown', (event) => {
    if (event.key === 's') {
        $('#shuffle').click();
    }
    if (event.key.toLowerCase() === 'm') {
        const music = document.getElementById('music');
        if (music.paused) music.play();
        else music.pause();
    }
});

function fixSrc(target) {
    target.src = target.getAttribute('src2');
}
