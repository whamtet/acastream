const el = document.getElementById("time-disp");
let secs = 120;

const interval = setInterval(() => {
    secs--;
    const minutes = Math.floor(secs / 60);
    const seconds = secs % 60;
    el.innerText = `${minutes}:${seconds}`;
    if (secs === 0) {
        clearInterval(interval);
    }
}, 1000);
