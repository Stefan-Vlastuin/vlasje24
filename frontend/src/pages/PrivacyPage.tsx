export function PrivacyPage() {
  return (
    <article className="mx-auto max-w-2xl rounded-xl bg-white p-6 shadow-sm">
      <h1 className="text-2xl font-bold text-gray-900">Privacy</h1>
      <p className="mt-2 text-sm text-gray-500">Laatst bijgewerkt: 17 oktober 2026</p>

      <div className="mt-6 space-y-5 text-sm leading-6 text-gray-700">
        <section>
          <h2 className="font-semibold text-gray-900">Welke gegevens worden verwerkt?</h2>
          <p>Voor een account bewaart Vlasje24 je gebruikersnaam, e-mailadres, beveiligd wachtwoord, accountstatus en tijdstippen. Ook worden je reacties en noodzakelijke beveiligingsgegevens, zoals sessies en tijdelijke rate-limitgegevens, verwerkt.</p>
        </section>
        <section>
          <h2 className="font-semibold text-gray-900">Waarom?</h2>
          <p>Accountgegevens zijn nodig om de door jou gevraagde accountdienst uit te voeren. Beveiliging en moderatie gebeuren vanuit het gerechtvaardigde belang om de website veilig en bruikbaar te houden. Vlasje24 gebruikt geen advertentie- of trackingcookies.</p>
        </section>
        <section>
          <h2 className="font-semibold text-gray-900">E-mail en andere partijen</h2>
          <p>De website en database draaien bij STRATO. Voor accountmails gebruikt Vlasje24 Brevo. Brevo ontvangt daarvoor je e-mailadres, gebruikersnaam, de accountmail en technische aflevergegevens. Wachtwoorden worden nooit met deze partijen gedeeld.</p>
        </section>
        <section>
          <h2 className="font-semibold text-gray-900">Bewaren en verwijderen</h2>
          <p>Je account blijft bestaan totdat je het verwijdert. Daarna worden je e-mailadres, gebruikersnaam en reactie-inhoud direct geanonimiseerd en worden je sessies en accounttokens verwijderd. Verlopen of gebruikte tokens worden uiterlijk zeven dagen later opgeruimd. Verwijderde reacties worden maximaal 90 dagen bewaard, moderatiegegevens maximaal één jaar en beveiligde databaseback-ups maximaal 30 dagen.</p>
        </section>
        <section>
          <h2 className="font-semibold text-gray-900">Je rechten en contact</h2>
          <p>Via je account kun je zelf verwijderen. Voor inzage, correctie, bezwaar of andere privacyvragen kun je antwoorden op een accountmail van Vlasje24. Je kunt daarnaast een klacht indienen bij de Autoriteit Persoonsgegevens.</p>
        </section>
      </div>
    </article>
  )
}
