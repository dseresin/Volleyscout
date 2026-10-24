package com.example.volleyscout

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

// --- MODELLI DATI ---
data class Player(
    val number: Int,
    var name: String
)

enum class Skill(val label: String) {
    BATTUTA("Battuta"),
    RICEZIONE("Ricezione"),
    ATTACCO("Attacco")
}

enum class Rating(val symbol: String, val label: String, val color: Color) {
    PERFECT("#", "Punto / Perfetta", Color(0xFF2E7D32)), // Verde scuro
    GOOD("+", "Positiva", Color(0xFF4CAF50)),            // Verde chiaro
    NEUTRAL("!", "Neutra", Color(0xFFFFB300)),           // Giallo/Arancio
    BAD("-", "Negativa", Color(0xFFFB8C00)),             // Arancio scuro
    ERROR("=", "Errore", Color(0xFFD32F2F))              // Rosso
}

data class ScoutEvent(
    val id: Int,
    val setNumber: Int,
    val player: Player?,
    val isOpponent: Boolean,
    val skill: Skill?,
    val rating: Rating?,
    val description: String
)

// --- ACTIVITY PRINCIPALE ---
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    VolleyScoutTabletApp()
                }
            }
        }
    }
}

// --- INTERFACCIA PRINCIPALE PER TABLET ---
@Composable
fun VolleyScoutTabletApp() {
    val context = LocalContext.current

    // Roster completo con Nomi e Numeri
    val fullRoster = remember {
        mutableStateListOf(
            Player(1, "Martina"),
            Player(2, "Giulia"),
            Player(3, "Sofia"),
            Player(5, "Chiara"),
            Player(7, "Francesca"),
            Player(8, "Sara"),
            Player(10, "Elena"),
            Player(11, "Alice"),
            Player(13, "Giorgia"),
            Player(14, "Beatrice"),
            Player(15, "Camilla"),
            Player(18, "Greta")
        )
    }

    // I 6 giocatori in campo
    val courtPlayers = remember {
        mutableStateListOf<Player>().apply {
            addAll(fullRoster.take(6))
        }
    }

    var currentSet by remember { mutableStateOf(1) }

    var selectedPlayer by remember { mutableStateOf<Player?>(null) }
    var selectedSkill by remember { mutableStateOf<Skill?>(null) }
    val eventsList = remember { mutableStateListOf<ScoutEvent>() }
    var eventCounter by remember { mutableStateOf(1) }

    // Dialogs
    var showRosterDialog by remember { mutableStateOf(false) }
    var showSubDialog by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }
    var playerToSubOut by remember { mutableStateOf<Player?>(null) }

    val skillList = Skill.entries
    val ratingList = Rating.entries

    Row(modifier = Modifier.fillMaxSize().padding(12.dp)) {

        // COLONNA DI SINISTRA: Pannello di Rilevazione
        Column(
            modifier = Modifier
                .weight(2f)
                .fillMaxHeight()
                .padding(end = 8.dp)
        ) {
            // RIGA 1: Titolo e Pulsanti Azione (Roster e Cambi BEN VISIBILI)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("VOLLEY SCOUT", fontSize = 20.sp, fontWeight = FontWeight.Bold)

                Row {
                    Button(
                        onClick = { showRosterDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF455A64)),
                        modifier = Modifier.padding(end = 6.dp)
                    ) {
                        Text("📋 Gestisci Roster", fontSize = 13.sp)
                    }

                    Button(
                        onClick = { showSubDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF607D8B))
                    ) {
                        Text("🔄 Sostituzioni", fontSize = 13.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // RIGA 2: Selettore del Set Attuale
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            ) {
                Text("SET ATTUALE: ", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.width(8.dp))
                (1..5).forEach { setNum ->
                    FilterChip(
                        selected = (currentSet == setNum),
                        onClick = { currentSet = setNum },
                        label = { Text("Set $setNum") },
                        modifier = Modifier.padding(horizontal = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // STEP 1: Selezione Giocatore (SOLO IN CAMPO)
            Text("1. Seleziona Giocatore in Campo:", fontWeight = FontWeight.SemiBold)
            Row(
                modifier = Modifier.fillMaxWidth().height(85.dp).padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                courtPlayers.forEach { player ->
                    val isSelected = selectedPlayer?.number == player.number
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(3.dp)
                            .background(
                                color = if (isSelected) Color(0xFF1976D2) else Color(0xFFE0E0E0),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { selectedPlayer = player }
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "#${player.number}",
                                color = if (isSelected) Color.White else Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            Text(
                                text = player.name,
                                color = if (isSelected) Color.White else Color.DarkGray,
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // STEP 2: Selezione Fondamentale
            Text("2. Seleziona Fondamentale:", fontWeight = FontWeight.SemiBold)
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                skillList.forEach { skill ->
                    val isSelected = selectedSkill == skill
                    Button(
                        onClick = { selectedSkill = skill },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) Color(0xFF0288D1) else Color(0xFFB0BEC5)
                        ),
                        modifier = Modifier.weight(1f).padding(2.dp)
                    ) {
                        Text(skill.label, fontSize = 15.sp, color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // STEP 3: Valutazione e Inserimento Evento
            Text("3. Valutazione (Registra Azione):", fontWeight = FontWeight.SemiBold)
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                ratingList.forEach { rating ->
                    Button(
                        onClick = {
                            if (selectedPlayer != null && selectedSkill != null) {
                                eventsList.add(
                                    0,
                                    ScoutEvent(
                                        id = eventCounter++,
                                        setNumber = currentSet,
                                        player = selectedPlayer,
                                        isOpponent = false,
                                        skill = selectedSkill,
                                        rating = rating,
                                        description = "Set $currentSet | #${selectedPlayer!!.number} ${selectedPlayer!!.name} ${selectedSkill!!.label} -> ${rating.symbol}"
                                    )
                                )
                                selectedPlayer = null
                                selectedSkill = null
                            }
                        },
                        enabled = (selectedPlayer != null && selectedSkill != null),
                        colors = ButtonDefaults.buttonColors(containerColor = rating.color),
                        modifier = Modifier.weight(1f).padding(2.dp).height(48.dp)
                    ) {
                        Text(rating.symbol, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // PULSANTE PER LA SQUADRA AVVERSARIA
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text("Squadra Avversaria (Set $currentSet)", fontWeight = FontWeight.Bold, color = Color(0xFFC62828))
                    Spacer(modifier = Modifier.height(4.dp))
                    Button(
                        onClick = {
                            eventsList.add(
                                0,
                                ScoutEvent(
                                    id = eventCounter++,
                                    setNumber = currentSet,
                                    player = null,
                                    isOpponent = true,
                                    skill = Skill.BATTUTA,
                                    rating = Rating.ERROR,
                                    description = "Set $currentSet | ⚠️ Errore Battuta Avversario (+1 punto a noi)"
                                )
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                        modifier = Modifier.fillMaxWidth().height(44.dp)
                    ) {
                        Text("⚠️ ERRORE BATTUTA AVVERSARIO", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        VerticalDivider(modifier = Modifier.fillMaxHeight().width(1.dp), color = Color.Gray)

        // COLONNA DI DESTRA: Cronologia ed Esportazione
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(start = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Cronologia", fontSize = 18.sp, fontWeight = FontWeight.Bold)

                Button(
                    onClick = { showReportDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                ) {
                    Text("📊 Report")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            val oppErrorsCurrentSet = eventsList.count { it.isOpponent && it.setNumber == currentSet }
            Text("Err. battuta avv. Set $currentSet: $oppErrorsCurrentSet", fontSize = 14.sp, color = Color.DarkGray)

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(eventsList) { event ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (event.isOpponent) Color(0xFFFFCDD2) else Color(0xFFF5F5F5)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = event.description,
                                fontSize = 12.sp,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "X",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Red,
                                modifier = Modifier.clickable { eventsList.remove(event) }
                            )
                        }
                    }
                }
            }
        }
    }

    // --- FINESTRA DIALOGO GESTIONE ROSTER / NOMI ---
    if (showRosterDialog) {
        Dialog(onDismissRequest = { showRosterDialog = false }) {
            Card(modifier = Modifier.fillMaxWidth().padding(12.dp), shape = RoundedCornerShape(12.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Gestione Rosa / Roster Squadra", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("Modifica i nomi delle giocatrici per ciascun numero:", fontSize = 12.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(modifier = Modifier.height(280.dp)) {
                        items(fullRoster.indices.toList()) { index ->
                            val p = fullRoster[index]
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("#${p.number}", fontWeight = FontWeight.Bold, modifier = Modifier.width(40.dp))
                                OutlinedTextField(
                                    value = p.name,
                                    onValueChange = { newName ->
                                        fullRoster[index] = p.copy(name = newName)
                                    },
                                    label = { Text("Nome Giocatrice") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { showRosterDialog = false },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Salva e Chiudi")
                    }
                }
            }
        }
    }

    // --- FINESTRA DIALOGO SOSTITUZIONI ---
    if (showSubDialog) {
        Dialog(onDismissRequest = { showSubDialog = false; playerToSubOut = null }) {
            Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(12.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Gestione Sostituzioni", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    if (playerToSubOut == null) {
                        Text("1. Chi esce dal campo?", fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            courtPlayers.forEach { player ->
                                Button(
                                    onClick = { playerToSubOut = player },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                                ) {
                                    Text("#${player.number} ${player.name}", fontSize = 13.sp)
                                }
                            }
                        }
                    } else {
                        Text("2. Chi entra al posto di #${playerToSubOut!!.number} ${playerToSubOut!!.name}?", fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(8.dp))
                        val benchPlayers = fullRoster.filter { b -> courtPlayers.none { c -> c.number == b.number } }
                        LazyVerticalGrid(columns = GridCells.Fixed(3), modifier = Modifier.height(140.dp)) {
                            items(benchPlayers) { benchPlayer ->
                                Button(
                                    onClick = {
                                        val index = courtPlayers.indexOfFirst { it.number == playerToSubOut!!.number }
                                        if (index != -1) courtPlayers[index] = benchPlayer
                                        eventsList.add(
                                            0,
                                            ScoutEvent(
                                                id = eventCounter++,
                                                setNumber = currentSet,
                                                player = benchPlayer,
                                                isOpponent = false,
                                                skill = null,
                                                rating = null,
                                                description = "Set $currentSet | 🔄 Cambio: Esce #${playerToSubOut!!.number} ${playerToSubOut!!.name} / Entra #${benchPlayer.number} ${benchPlayer.name}"
                                            )
                                        )
                                        showSubDialog = false
                                        playerToSubOut = null
                                        selectedPlayer = null
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                    modifier = Modifier.padding(4.dp)
                                ) {
                                    Text("#${benchPlayer.number} ${benchPlayer.name}", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // --- FINESTRA DIALOGO REPORT STATISTICHE ED ESPORTAZIONE ---
    if (showReportDialog) {
        val csvData = generateCsvReport(eventsList)

        Dialog(onDismissRequest = { showReportDialog = false }) {
            Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(12.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Statistiche Partita divise per Set", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Punti regalati da Errori Battuta Avversari:", fontWeight = FontWeight.Bold)
                    (1..5).forEach { s ->
                        val err = eventsList.count { it.isOpponent && it.setNumber == s }
                        if (err > 0) Text(" • Set $s: $err punti", fontSize = 13.sp)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            val sendIntent: Intent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, csvData)
                                type = "text/plain"
                            }
                            val shareIntent = Intent.createChooser(sendIntent, "Esporta Tabella Statistiche")
                            context.startActivity(shareIntent)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("📤 Condividi / Invia File CSV (Excel)")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(
                        onClick = { showReportDialog = false },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Chiudi")
                    }
                }
            }
        }
    }
}

// --- FUNZIONE PER GENERARE IL CSV DIVISO PER SET ---
fun generateCsvReport(events: List<ScoutEvent>): String {
    val sb = StringBuilder()
    sb.append("Numero;Nome;Set;Bat Tot;Bat Ace(#);Bat Err(=);Ric Tot;Ric Perf(#);Ric Pos(+);Ric Err(=);Att Tot;Att Punti(#);Att Err(=)\n")

    val playerEvents = events.filter { !it.isOpponent && it.player != null }
    val players = playerEvents.mapNotNull { it.player }.distinctBy { it.number }.sortedBy { it.number }

    for (p in players) {
        for (s in 1..5) {
            val pSetEvents = playerEvents.filter { it.player?.number == p.number && it.setNumber == s }
            if (pSetEvents.isNotEmpty()) {
                val batTot = pSetEvents.count { it.skill == Skill.BATTUTA }
                val batAce = pSetEvents.count { it.skill == Skill.BATTUTA && it.rating == Rating.PERFECT }
                val batErr = pSetEvents.count { it.skill == Skill.BATTUTA && it.rating == Rating.ERROR }

                val ricTot = pSetEvents.count { it.skill == Skill.RICEZIONE }
                val ricPerf = pSetEvents.count { it.skill == Skill.RICEZIONE && it.rating == Rating.PERFECT }
                val ricPos = pSetEvents.count { it.skill == Skill.RICEZIONE && it.rating == Rating.GOOD }
                val ricErr = pSetEvents.count { it.skill == Skill.RICEZIONE && it.rating == Rating.ERROR }

                val attTot = pSetEvents.count { it.skill == Skill.ATTACCO }
                val attPunti = pSetEvents.count { it.skill == Skill.ATTACCO && it.rating == Rating.PERFECT }
                val attErr = pSetEvents.count { it.skill == Skill.ATTACCO && it.rating == Rating.ERROR }

                sb.append("#${p.number};${p.name};Set $s;$batTot;$batAce;$batErr;$ricTot;$ricPerf;$ricPos;$ricErr;$attTot;$attPunti;$attErr\n")
            }
        }
    }

    sb.append("\n--- ERRORI BATTUTA AVVERSARI ---\n")
    for (s in 1..5) {
        val oppErr = events.count { it.isOpponent && it.setNumber == s }
        if (oppErr > 0) {
            sb.append("Set $s; $oppErr punti regalati\n")
        }
    }

    return sb.toString()
}