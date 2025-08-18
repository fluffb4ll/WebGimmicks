const confirm = document.getElementById("confirm");
const linkInput = document.getElementById("link");
confirm.addEventListener("click", compressLink);

linkInput.addEventListener("input", () => {
    confirm.disabled = linkInput.value.length === 0;
});

async function compressLink() {
    const link = linkInput.value;

    const response = await fetch("api/ls", {
        method: "POST",
        body: link.replace('https://', '').replace('http://', '')
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
            let linkForUser = document.createElement('a');
            linkForUser.id = "shortLink"
            linkForUser.href = shortLink;

            let linkText = document.createElement('span');
            linkText.id = 'shortLinkText';
            linkText.innerText = shortLink;

            linkForUser.appendChild(linkText);

            let linkContainer = document.createElement('div');
            linkContainer.id = "shortLinkContainer";
            linkContainer.appendChild(linkForUser);

            let copyButton = document.createElement('button');
            copyButton.id = "copyButton";
            copyButton.type = "button";
            copyButton.addEventListener("click", copyLink);

            let buttonName = document.createElement('span');
            buttonName.id = "copyButtonName"
            buttonName.innerText = "copy dat shid"

            let tooltip = document.createElement('span');
            tooltip.id = "copyTooltip";
            tooltip.className = "tooltiptext";
            tooltip.innerText = "copied :D";

            copyButton.appendChild(buttonName);
            copyButton.appendChild(tooltip);

            let buttonContainer = document.createElement('div');
            buttonContainer.id = 'buttonContainer';
            buttonContainer.className = "tooltip"
            buttonContainer.appendChild(copyButton);

            let linkCard = document.createElement('div');
            linkCard.id = "shortLinkCard";
            linkCard.appendChild(linkContainer);
            linkCard.appendChild(buttonContainer);

            document.getElementById('mainWindow').appendChild(linkCard);
        } else {
            document.getElementById('shortLinkText').innerText = shortLink;
            document.getElementById('shortLink').href = shortLink;
        }
    }
}

async function copyLink() {
    let shortLink = document.getElementById('shortLink');
    const tooltip = document.getElementById('copyTooltip');
    navigator.clipboard.writeText(shortLink.innerText);
    tooltip.style.visibility = "visible";
    tooltip.style.opacity = 1;
    await delay(2000);
    tooltip.style.opacity = 0;
}

function delay(time) {
    return new Promise(resolve => {
        setTimeout(resolve, time);
    });
}