document.getElementById("confirm").addEventListener("click", compressLink);

async function compressLink() {
    const link = document.getElementById('link').value;

    const response = await fetch("api/ls", {
        method:"POST",
        body: link
    });

    if (!response.ok) {
        alert('Response status: ' + response.status);
    } else {
        const shortLink = window.location.href
                                .replace("http://", "")
                                .replace('https://', "")
                                .replace('?', '')
                                + "/" + await response.text();
        if (document.getElementById('shortLinkContainer') == null) {
            let linkForUser = document.createElement('p');
            linkForUser.id = 'shortLink';
            linkForUser.innerText = shortLink;

            let linkContainer = document.createElement('div');
            linkContainer.id = "shortLinkContainer";
            linkContainer.appendChild(linkForUser);

            document.getElementById('mainWindow').appendChild(linkContainer);
        } else {
            document.getElementById('shortLink').innerText = shortLink;
        }
    }
}