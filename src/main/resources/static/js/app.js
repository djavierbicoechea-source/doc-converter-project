function copyLink(btn){
  const link = btn.getAttribute("data-copy");
  if(!link) return;

  navigator.clipboard.writeText(link).then(() => {
    const original = btn.innerText;
    btn.innerText = "✅ Lien copié";
    setTimeout(() => btn.innerText = original, 1200);
  }).catch(() => {
    alert("Copie impossible. Voici le lien: " + link);
  });
}
