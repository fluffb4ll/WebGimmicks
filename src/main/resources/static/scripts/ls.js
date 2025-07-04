document.getElementById("confirm").addEventListener("click", compressLink);

async function compressLink() {
    const link = document.getElementById('link').value;

    const response = await fetch("api/ls", {
        method:"POST",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify({link: link})
    });

}