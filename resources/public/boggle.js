const el = document.getElementById("time-disp");
let secs = 120;

const interval = setInterval(() => {
    secs--;
    const minutes = Math.floor(secs / 60);
    let seconds = secs % 60;
    if (seconds < 10) {
        seconds = '0' + seconds;
    }
    el.innerText = `${minutes}:${seconds}`;
    if (secs === 0) {
        clearInterval(interval);
    }
}, 1000);
