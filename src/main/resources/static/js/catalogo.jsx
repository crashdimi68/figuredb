/*
 * FigureDB - Catalogo figure
 * ---------------------------------------------------------------------------
 * Componente React che realizza la parte interattiva del frontend.
 * I dati arrivano dal backend Spring Boot attraverso le API REST:
 *
 *   GET /api/figure?q=&serieId=&aziendaId=&materiale=&soloLimitate=&prezzoMin=&prezzoMax=
 *   GET /api/serie
 *   GET /api/aziende
 *   GET /api/materiali
 *   GET /api/figure/{id}/recensioni
 *
 * Nota: e' la parte del frontend dove React porta un vantaggio reale rispetto
 * a Thymeleaf, perche' i filtri si combinano e si applicano senza ricaricare
 * la pagina.
 */

const { useState, useEffect, useCallback, useMemo } = React;

/** Legge il token CSRF dal cookie impostato da Spring Security. */
function leggiTokenCsrf() {
    const match = document.cookie.match(/(^|;)\s*XSRF-TOKEN\s*=\s*([^;]+)/);
    return match ? decodeURIComponent(match[2]) : null;
}

function Stelle({ voto }) {
    const pieno = Math.round(voto || 0);
    return (
        <span className="stelle" title={`${(voto || 0).toFixed(1)} su 5`}>
            {"★".repeat(pieno)}{"☆".repeat(5 - pieno)}
        </span>
    );
}

function Filtri({ filtri, onCambia, serie, aziende, materiali, onAzzera }) {
    const aggiorna = (campo) => (evento) => {
        const target = evento.target;
        const valore = target.type === "checkbox" ? target.checked : target.value;
        onCambia({ ...filtri, [campo]: valore });
    };

    return (
        <aside className="filtri">
            <h2>Filtri</h2>

            <label htmlFor="f-q">Ricerca libera</label>
            <input id="f-q" type="search" value={filtri.q}
                   placeholder="nome, serie o azienda"
                   onChange={aggiorna("q")} />

            <label htmlFor="f-serie">Serie</label>
            <select id="f-serie" value={filtri.serieId} onChange={aggiorna("serieId")}>
                <option value="">Tutte</option>
                {serie.map((s) => <option key={s.id} value={s.id}>{s.nome}</option>)}
            </select>

            <label htmlFor="f-azienda">Azienda</label>
            <select id="f-azienda" value={filtri.aziendaId} onChange={aggiorna("aziendaId")}>
                <option value="">Tutte</option>
                {aziende.map((a) => <option key={a.id} value={a.id}>{a.nome}</option>)}
            </select>

            <label htmlFor="f-materiale">Materiale</label>
            <select id="f-materiale" value={filtri.materiale} onChange={aggiorna("materiale")}>
                <option value="">Tutti</option>
                {materiali.map((m) => <option key={m} value={m}>{m}</option>)}
            </select>

            <div className="filtri-prezzo">
                <div>
                    <label htmlFor="f-min">Prezzo min</label>
                    <input id="f-min" type="number" min="0" step="1"
                           value={filtri.prezzoMin} onChange={aggiorna("prezzoMin")} />
                </div>
                <div>
                    <label htmlFor="f-max">Prezzo max</label>
                    <input id="f-max" type="number" min="0" step="1"
                           value={filtri.prezzoMax} onChange={aggiorna("prezzoMax")} />
                </div>
            </div>

            <label className="checkbox">
                <input type="checkbox" checked={filtri.soloLimitate}
                       onChange={aggiorna("soloLimitate")} />
                Solo edizioni limitate
            </label>

            <button type="button" className="bottone secondario" onClick={onAzzera}>
                Azzera filtri
            </button>
        </aside>
    );
}

function SchedaFigure({ figure, onApri }) {
    return (
        <article className="card" onClick={() => onApri(figure)}>
            {figure.immagine
                ? <img src={figure.immagine} alt={figure.nome} loading="lazy" />
                : <div className="card-placeholder">Nessuna immagine</div>}
            <div className="card-corpo">
                <h3>{figure.nome}</h3>
                <p className="meta">{figure.aziendaNome} &middot; {figure.serieNome}</p>
                <p className="meta">{figure.altezza} cm &middot; {figure.materiale}</p>
                <p className="prezzo">{figure.prezzo.toFixed(2)} EUR</p>
                {figure.edizioneLimitata && <span className="badge">Edizione limitata</span>}
            </div>
        </article>
    );
}

function PannelloDettaglio({ figure, onChiudi }) {
    const [recensioni, setRecensioni] = useState([]);
    const [caricamento, setCaricamento] = useState(true);

    useEffect(() => {
        let annullato = false;
        setCaricamento(true);
        fetch(`/api/figure/${figure.id}/recensioni`)
            .then((r) => (r.ok ? r.json() : []))
            .then((dati) => { if (!annullato) { setRecensioni(dati); setCaricamento(false); } })
            .catch(() => { if (!annullato) { setRecensioni([]); setCaricamento(false); } });
        return () => { annullato = true; };
    }, [figure.id]);

    return (
        <div className="pannello-overlay" onClick={onChiudi}>
            <div className="pannello" onClick={(e) => e.stopPropagation()}>
                <button className="chiudi" onClick={onChiudi} aria-label="Chiudi">&times;</button>
                <h2>{figure.nome}</h2>
                <p className="meta">{figure.aziendaNome} &middot; {figure.serieNome}</p>
                {figure.immagine && <img src={figure.immagine} alt={figure.nome} />}
                <dl>
                    <dt>Uscita</dt><dd>{figure.dataUscita}</dd>
                    <dt>Altezza</dt><dd>{figure.altezza} cm</dd>
                    <dt>Materiale</dt><dd>{figure.materiale}</dd>
                    <dt>Prezzo</dt><dd>{figure.prezzo.toFixed(2)} EUR</dd>
                </dl>
                {figure.descrizione && <p>{figure.descrizione}</p>}

                <h3>Recensioni</h3>
                {caricamento && <p>Caricamento...</p>}
                {!caricamento && recensioni.length === 0 && <p>Nessuna recensione.</p>}
                <ul className="lista-recensioni">
                    {recensioni.map((r) => (
                        <li key={r.id}>
                            <strong>{r.titolo}</strong> <Stelle voto={r.voto} />
                            <p className="meta">{r.autore} &middot; {r.data}</p>
                            <p>{r.testo}</p>
                        </li>
                    ))}
                </ul>

                <a className="bottone" href={`/figure/${figure.id}`}>
                    Apri la scheda completa
                </a>
            </div>
        </div>
    );
}

/**
 * Barra di navigazione fra le pagine dei risultati.
 * Le figure sono gia' tutte in memoria (le ha filtrate il backend), quindi
 * qui la paginazione serve a non riversare centinaia di card nel DOM in una
 * volta sola: si cambia pagina senza nessuna nuova richiesta al server.
 */
function Paginazione({ pagina, totalePagine, onCambia }) {
    if (totalePagine <= 1) return null;

    const numeri = Array.from({ length: totalePagine }, (_, i) => i);

    return (
        <nav className="paginazione">
            <button type="button" className="link-pagina"
                    disabled={pagina === 0}
                    onClick={() => onCambia(pagina - 1)}>&laquo; Precedente</button>

            {numeri.map((i) => (
                <button key={i} type="button"
                        className={"link-pagina" + (i === pagina ? " corrente" : "")}
                        onClick={() => onCambia(i)}>{i + 1}</button>
            ))}

            <button type="button" className="link-pagina"
                    disabled={pagina === totalePagine - 1}
                    onClick={() => onCambia(pagina + 1)}>Successiva &raquo;</button>
        </nav>
    );
}

/** Quante figure mostrare per pagina. */
const PER_PAGINA = 12;

const FILTRI_INIZIALI = {
    q: "", serieId: "", aziendaId: "", materiale: "",
    soloLimitate: false, prezzoMin: "", prezzoMax: ""
};

function Catalogo() {
    const [filtri, setFiltri] = useState(FILTRI_INIZIALI);
    const [figure, setFigure] = useState([]);
    const [serie, setSerie] = useState([]);
    const [aziende, setAziende] = useState([]);
    const [materiali, setMateriali] = useState([]);
    const [caricamento, setCaricamento] = useState(true);
    const [errore, setErrore] = useState(null);
    const [selezionata, setSelezionata] = useState(null);
    const [ordinamento, setOrdinamento] = useState("nome");
    const [pagina, setPagina] = useState(0);

    // dati per i menu dei filtri: caricati una sola volta
    useEffect(() => {
        Promise.all([
            fetch("/api/serie").then((r) => r.json()),
            fetch("/api/aziende").then((r) => r.json()),
            fetch("/api/materiali").then((r) => r.json())
        ])
            .then(([s, a, m]) => { setSerie(s); setAziende(a); setMateriali(m); })
            .catch(() => setErrore("Impossibile caricare i dati dei filtri."));
    }, []);

    const costruisciQuery = useCallback((f) => {
        const parametri = new URLSearchParams();
        if (f.q.trim()) parametri.set("q", f.q.trim());
        if (f.serieId) parametri.set("serieId", f.serieId);
        if (f.aziendaId) parametri.set("aziendaId", f.aziendaId);
        if (f.materiale) parametri.set("materiale", f.materiale);
        if (f.soloLimitate) parametri.set("soloLimitate", "true");
        if (f.prezzoMin) parametri.set("prezzoMin", f.prezzoMin);
        if (f.prezzoMax) parametri.set("prezzoMax", f.prezzoMax);
        return parametri.toString();
    }, []);

    // ricerca con debounce: evita una richiesta ad ogni tasto premuto
    useEffect(() => {
        const timer = setTimeout(() => {
            setCaricamento(true);
            setErrore(null);
            fetch(`/api/figure?${costruisciQuery(filtri)}`)
                .then((r) => {
                    if (!r.ok) throw new Error(`HTTP ${r.status}`);
                    return r.json();
                })
                .then((dati) => { setFigure(dati); setCaricamento(false); })
                .catch(() => { setErrore("Errore nel caricamento del catalogo."); setCaricamento(false); });
        }, 300);
        return () => clearTimeout(timer);
    }, [filtri, costruisciQuery]);

    // Cambiando filtri o ordinamento l'insieme dei risultati e' un altro:
    // restare alla pagina 5 non avrebbe senso, si riparte dalla prima.
    useEffect(() => { setPagina(0); }, [filtri, ordinamento]);

    const figureOrdinate = useMemo(() => {
        const copia = [...figure];
        if (ordinamento === "prezzoAsc") copia.sort((a, b) => a.prezzo - b.prezzo);
        else if (ordinamento === "prezzoDesc") copia.sort((a, b) => b.prezzo - a.prezzo);
        else if (ordinamento === "recenti") copia.sort((a, b) => b.dataUscita.localeCompare(a.dataUscita));
        else copia.sort((a, b) => a.nome.localeCompare(b.nome));
        return copia;
    }, [figure, ordinamento]);

    const totalePagine = Math.max(Math.ceil(figureOrdinate.length / PER_PAGINA), 1);
    // Se i filtri riducono i risultati la pagina corrente puo' finire fuori
    // dall'intervallo: la si riporta dentro invece di mostrare una griglia vuota.
    const paginaCorrente = Math.min(pagina, totalePagine - 1);
    const figureDellaPagina = figureOrdinate.slice(
        paginaCorrente * PER_PAGINA, paginaCorrente * PER_PAGINA + PER_PAGINA);

    return (
        <div className="catalogo">
            <Filtri filtri={filtri} onCambia={setFiltri} serie={serie}
                    aziende={aziende} materiali={materiali}
                    onAzzera={() => setFiltri(FILTRI_INIZIALI)} />

            <section className="risultati">
                <div className="barra-risultati">
                    <span>{caricamento
                        ? "Ricerca..."
                        : (figureOrdinate.length > 0
                            ? `Pagina ${paginaCorrente + 1} di ${totalePagine}`
                            : "")}</span>
                    <select value={ordinamento} onChange={(e) => setOrdinamento(e.target.value)}>
                        <option value="nome">Nome (A-Z)</option>
                        <option value="prezzoAsc">Prezzo crescente</option>
                        <option value="prezzoDesc">Prezzo decrescente</option>
                        <option value="recenti">Uscite piu' recenti</option>
                    </select>
                </div>

                {errore && <p className="alert errore">{errore}</p>}
                {!caricamento && figureOrdinate.length === 0 && !errore &&
                    <p>Nessuna figure corrisponde ai filtri selezionati.</p>}

                <div className="griglia">
                    {figureDellaPagina.map((f) =>
                        <SchedaFigure key={f.id} figure={f} onApri={setSelezionata} />)}
                </div>

                <Paginazione pagina={paginaCorrente} totalePagine={totalePagine}
                             onCambia={(p) => {
                                 setPagina(p);
                                 window.scrollTo({ top: 0, behavior: "smooth" });
                             }} />
            </section>

            {selezionata &&
                <PannelloDettaglio figure={selezionata} onChiudi={() => setSelezionata(null)} />}
        </div>
    );
}

ReactDOM.createRoot(document.getElementById("root-catalogo")).render(<Catalogo />);
