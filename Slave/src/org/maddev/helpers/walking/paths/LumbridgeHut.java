package org.maddev.helpers.walking.paths;

import org.maddev.helpers.walking.CustomPath;
import org.maddev.tasks.hunting.MuseumQuiz;
import org.rspeer.runetek.api.commons.BankLocation;
import org.rspeer.runetek.api.movement.Movement;
import org.rspeer.runetek.api.movement.position.Position;

public class LumbridgeHut extends CustomPath {

    private Position LUMBRIDGE_HOUSE = new Position(3230, 3235, 0);
    private Position OUTSIDE_LUMBRIDGE_HOUSE = new Position(3230, 3232, 0);

    private Position[] PATH_PAST_LUMBRDIGE_HOUSE = {
                new Position(3222, 3218, 0),
                new Position(3235, 3227, 0),
                new Position(3218, 3247, 0),
                new Position(3216, 3261, 0),
                new Position(3243, 3261, 0),
                new Position(3245, 3273, 0),
                new Position(3238, 3284, 0)
    };

    @Override
    public boolean validate(Position end) {
        // Do not validate this custom path if we are not trying to walk towards the grand exchange.
        if(end.distance(BankLocation.GRAND_EXCHANGE.getPosition()) > 5
                && end.distance(MuseumQuiz.MUSEUM_AREA.getCenter()) > 5) {
            return false;
        }
        if(!LUMBRIDGE_HOUSE.isLoaded()) {
            return false;
        }
        if(!Movement.isDestinationSet()) {
          return false;
        }
        Position dest = Movement.getDestination();
        return dest.distance(LUMBRIDGE_HOUSE) <= 5 || dest.distance(OUTSIDE_LUMBRIDGE_HOUSE) <= 3;
    }

    @Override
    public Position[] getPositions() {
        return PATH_PAST_LUMBRDIGE_HOUSE;
    }

    @Override
    public String getName() {
        return "Lumbridge Hut";
    }
}
