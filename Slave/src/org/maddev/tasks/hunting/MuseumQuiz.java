package org.maddev.tasks.hunting;

import org.maddev.State;
import org.maddev.Store;
import org.maddev.helpers.interact.InteractHelper;
import org.maddev.helpers.walking.MovementHelper;
import org.rspeer.runetek.adapter.component.InterfaceComponent;
import org.rspeer.runetek.adapter.scene.Npc;
import org.rspeer.runetek.adapter.scene.Player;
import org.rspeer.runetek.adapter.scene.SceneObject;
import org.rspeer.runetek.api.Game;
import org.rspeer.runetek.api.Varps;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.commons.math.Random;
import org.rspeer.runetek.api.component.Dialog;
import org.rspeer.runetek.api.component.Interfaces;
import org.rspeer.runetek.api.component.tab.Skill;
import org.rspeer.runetek.api.component.tab.Skills;
import org.rspeer.runetek.api.movement.position.Area;
import org.rspeer.runetek.api.movement.position.Position;
import org.rspeer.runetek.api.scene.Npcs;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.runetek.api.scene.SceneObjects;
import org.rspeer.runetek.event.listeners.ChatMessageListener;
import org.rspeer.runetek.event.types.ChatMessageEvent;
import org.rspeer.script.task.Task;
import org.rspeer.ui.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class MuseumQuiz extends Task implements ChatMessageListener {

    private QuizDisplay animal;
    private static final int quizVarp = 1010;
    private static final int completedVarp = 1014;
    private Player me;
    private InterfaceComponent questionScreen;

    public static final Area MUSEUM_AREA = Area.rectangular(new Position(3254, 3447, 0), new Position(3258, 3455, 0));
    private static final Position ORLANDO_POSITION = new Position(1712, 4912);

    private static List<String> list = new ArrayList<>();
    private static final Predicate<? super InterfaceComponent> w = (w -> list.contains(w.getText()));

    @Override
    public boolean validate() {
        if(isDone() && inBasement()) {
            return true;
        }
        if(Skills.getCurrentLevel(Skill.HUNTER) >= 9) {
            return false;
        }
        return !isDone() || inBasement();
    }

    @Override
    public int execute() {
        doExecute();
        return Random.nextInt(350, 650);
    }

    private void doExecute() {
        Store.setTask("Solving quiz.");
        if(isDone() && inBasement()) {
            Store.setAction("Exiting basement.");
            exitBasement();
            return;
        }
        Store.setAction("Solving quiz.");
        questionScreen = Interfaces.getComponent(533, 28);
        me = Players.getLocal();
        animal = QuizDisplay.getCurrent();

        if (Dialog.canContinue()) {
            Dialog.processContinue();
            return;
        }
        if (!inBasement()) {
            getToBasement();
            return;
        }
        if (didFinishQuiz()) {
            talkToOrlando();
            list.clear();
            Log.info("Finished Museum Quiz.");
            return;
        }
        if (!startedQuiz()) {
            startQuiz();
            return;
        }
        solve();
    }

    public MuseumQuiz() {
        Game.getEventDispatcher().register(this);
        /* Lizard */
        list.add("Sunlight.");
        list.add("The Slayer Masters.");
        list.add("Three.");
        list.add("Squamata.");
        list.add("It becomes sleepy.");
        list.add("Hair.");
        /* Tortoise */
        list.add("Mibbiwocket.");
        list.add("Vegetables.");
        list.add("Admiral Bake.");
        list.add("Hard shell.");
        list.add("Twenty years.");
        list.add("Gnomes.");
        /* Dragon */
        list.add("Runite.");
        list.add("Anti dragon-breath shield.");
        list.add("Unknown.");
        list.add("Elemental.");
        list.add("Old battle sites.");
        list.add("Twelve.");
        /* Wyvern */
        list.add("Climate change.");
        list.add("Two.");
        list.add("Asgarnia.");
        list.add("Reptiles.");
        list.add("Dragons.");
        list.add("Below room temperature.");
        /* Snail */
        list.add("It is resistant to acid.");
        list.add("Spitting acid.");
        list.add("Fireproof oil.");
        list.add("Acid-spitting snail.");
        list.add("Contracting and stretching.");
        list.add("An operculum.");
        /* Snake */
        list.add("Stomach acid.");
        list.add("Tongue.");
        list.add("Seeing how you smell.");
        list.add("Constriction.");
        list.add("Squamata.");
        list.add("Anywhere.");
        /* Slug */
        list.add("Nematocysts.");
        list.add("The researchers keep vanishing.");
        list.add("Seaweed.");
        list.add("Defense or display.");
        list.add("Defence or display.");
        list.add("Ardougne.");
        list.add("They have a hard shell.");
        /* Monkey */
        list.add("Simian.");
        list.add("Harmless.");
        list.add("Bitternuts.");
        list.add("Red.");
        list.add("Seaweed.");
        /* Kalphite */
        list.add("Pasha.");
        list.add("Worker.");
        list.add("Lamellae.");
        list.add("Carnivores.");
        list.add("Scabaras.");
        list.add("Scarab beetles.");
        /* Terrorbird */
        list.add("Anything.");
        list.add("Gnomes.");
        list.add("Eating plants.");
        list.add("Four.");
        list.add("Stones.");
        list.add("0.");
        /* Penguin */
        list.add("Sight.");
        list.add("Planning.");
        list.add("A layer of fat.");
        list.add("Cold.");
        list.add("Social.");
        list.add("During breeding.");
        /* Mole */
        list.add("Subterranean.");
        list.add("Insects and other invertebrates.");
        list.add("They dig holes.");
        list.add("A labour.");
        list.add("Wyson the Gardener.");
        list.add("The Talpidae family.");
        /* Camel */
        list.add("Toxic dung.");
        list.add("Two.");
        list.add("Omnivore.");
        list.add("Annoyed.");
        list.add("Al Kharid.");
        list.add("Milk.");
        /* Leech */
        list.add("Water.");
        list.add("'Y'-shaped.");
        list.add("Apples.");
        list.add("Environment.");
        list.add("They attack by jumping.");
        list.add("It doubles in size.");
    }

    private InterfaceComponent questionComponent() {
        return questionScreen;
    }

    public void notify(ChatMessageEvent event) {
        if(Store.getState() == State.SCRIPT_STOPPED) {
            Game.getEventDispatcher().deregister(this);
            return;
        }
    }

    private void getToBasement() {
        if (!MUSEUM_AREA.contains(me)) {
            walkToMuseum();
        } else {
            goDownStairs();
        }
    }

    private void walkToDisplay() {
        if (atDisplay()) {
            openQuiz();
        } else {
            MovementHelper.walkRandomized(animal.getDisplay(), false);
            Time.sleepUntil(() -> me.getPosition().equals(animal.getDisplay()), 4500);
        }
    }

    private void openQuiz() {
        SceneObject plaque = SceneObjects.getNearest("Plaque");
        if (plaque != null) {
            plaque.interact("Study");
            Time.sleepUntil(this::inQuiz, 3000);
        }
    }

    private void clickAnswer() {
        InterfaceComponent c = Interfaces.getFirst(w);
        if (c != null) {
            c.interact("Ok");
        } else {
            handleDialog();
        }
    }

    private void walkToMuseum() {
        MovementHelper.walkRandomized(MUSEUM_AREA.getCenter(), false);
        Time.sleep(250, 550);
    }

    private void exitBasement() {
        SceneObject stairs = SceneObjects.getFirstAt(new Position(1758, 4959, 0));
        Store.setAction("Exiting basement.");
        if (stairs == null) {
            MovementHelper.walkRandomized(ORLANDO_POSITION, false);
            Time.sleep(850, 1500);
            return;
        }
        InteractHelper.interact(stairs, "Walk-up");
        Time.sleepUntil(this::inBasement, 2500);
    }

    private void goDownStairs() {
        SceneObject stairs = SceneObjects.getFirstAt(new Position(3255, 3451));
        if (stairs == null) {
            Store.setAction("Unable to find museum stairs?");
            return;
        }
        InteractHelper.interact(stairs, "Walk-down");
        Time.sleepUntil(this::inBasement, 2500);
    }

    private void walkToOrlando() {
        MovementHelper.walkRandomized(ORLANDO_POSITION, false);
        Time.sleep(1200, 2000);
    }

    private void talkToOrlando() {
        Npc orlando = Npcs.getNearest("Orlando Smith");
        if (Dialog.isOpen()) {
            handleDialog();
            return;
        }
        if (orlando == null) {
            walkToOrlando();
            return;
        }
        InteractHelper.interact(orlando, "Talk-to");
        Time.sleepUntil(Dialog::isOpen, 3500);
    }

    private void solve() {
        if (!atDisplay()) {
            walkToDisplay();
            return;
        }
        if (Dialog.isOpen()) {
            handleDialog();
            return;
        }
        if (!inQuiz()) {
            openQuiz();
            return;
        }
        clickAnswer();
        Time.sleepUntil(Dialog::isOpen, 3000);
    }

    private void startQuiz() {
        if (Dialog.isOpen()) {
            handleDialog();
        } else {
            talkToOrlando();
        }
    }

    private void handleDialog() {
        if (Dialog.canContinue()) {
            Dialog.processContinue();
        } else {
            Dialog.process(e -> e.contains("Sure thing."));
        }
    }

    private boolean didFinishQuiz() {
        return Varps.get(quizVarp) == 2076;
    }

    private boolean startedQuiz() {
        return getProgress() != 0;
    }

    private int getProgress() {
        return Varps.get(completedVarp);
    }

    private boolean atDisplay() {
        return animal.getDisplay().distance() <= 2;
    }

    private boolean inQuiz() {
        return questionComponent() != null;
    }

    public boolean isDone() {
        return Varps.get(quizVarp) == 2076 && Skills.getCurrentLevel(Skill.HUNTER) >= 9;
    }

    public boolean inBasement() {
        return ORLANDO_POSITION.isLoaded();
    }
}
