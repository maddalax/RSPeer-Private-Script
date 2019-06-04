package org.maddev.tasks;

import org.maddev.Store;
import org.maddev.tasks.hunting.Hunting;
import org.maddev.tasks.hunting.MuseumQuiz;
import org.maddev.tasks.zanaris.Zanaris;
import org.rspeer.runetek.api.Game;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.script.task.Task;
import org.rspeer.script.task.TaskScript;
import org.maddev.helpers.log.Logger;

import java.util.ArrayList;
import java.util.List;

public class SubmitTasks extends Task {

    private boolean submittedTasks;
    private List<Task> submitted;
    private TaskScript instance;

    private Crafting crafting;
    private Woodcutting woodcutting;
    private GrandExchange exchange;
    private MuseumQuiz quiz;
    private Hunting hunting;
    private LostCity lostCity;

    public SubmitTasks(TaskScript instance) {
        this.instance = instance;
        this.crafting = new Crafting();
        this.woodcutting = new Woodcutting();
        this.exchange = new GrandExchange();
        this.quiz = new MuseumQuiz();
        this.hunting = new Hunting();
        this.lostCity = new LostCity();
        this.submitted = new ArrayList<>();
    }

    @Override
    public boolean validate() {
        return true;
    }

    @Override
    public int execute() {
        Store.setTask("Submitting Tasks");
        if(submittedTasks || !Game.isLoggedIn()) {
            return 200;
        }

        submitOnce(new SetRun());
        submitOnce(new DepositStartingItems());

        if(Players.getLocal().getY() > 9000) {
            submitOnce(lostCity);
        }

        if(Hunting.MIDDLE.distance() < 300) {
            submitOnce(hunting);
        }

        if(quiz.inBasement()) {
            submitOnce(quiz);
        }

        if(Woodcutting.TREES.isLoaded()) {
            submitOnce(woodcutting);
        }

        submitOnce(new EquipGlory());
        submitOnce(exchange);
        if(!quiz.isDone()) {
            submitOnce(quiz);
        }

        submitOnce(hunting);
        submitOnce(woodcutting);
        submitOnce(crafting);
        submitOnce(lostCity);
        submitOnce(new Zanaris());

        submittedTasks = true;
        instance.remove(this);
        submitted.clear();
        return 50;
    }

    private void submitOnce(Task task) {
        if(submitted.contains(task)) {
            return;
        }
        instance.submit(task);
    }


}
