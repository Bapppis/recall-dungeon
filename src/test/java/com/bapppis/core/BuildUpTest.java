package com.bapppis.core;

import com.bapppis.core.creature.Player;
import com.bapppis.core.property.Property;
import com.bapppis.core.util.ResistanceUtil;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class BuildUpTest {

    @Test
    public void defaultValuesAreZero() {
        Player p = new Player();
        for (ResBuildUp rb : ResBuildUp.values()) {
            assertEquals(0, p.getResBuildUp(rb), "Default buildup should be 0 for " + rb.name());
        }
    }

    @Test
    public void modifyClampAndImmunity() {
        Player p = new Player();
        // numeric modifications and clamping
        p.modifyResBuildUp(ResBuildUp.FIRE, 50);
        assertEquals(50, p.getResBuildUp(ResBuildUp.FIRE));
        // After reaching 100, overload triggers and resets to 0 (new behavior)
        p.modifyResBuildUp(ResBuildUp.FIRE, 60); // would go to 110 -> clamp 100, then overload resets to 0
        assertEquals(0, p.getResBuildUp(ResBuildUp.FIRE), "After overload at 100%, buildup resets to 0");
        p.modifyResBuildUp(ResBuildUp.FIRE, 70);
        assertEquals(70, p.getResBuildUp(ResBuildUp.FIRE));

        // absolute set and immunity
        p.setResBuildUpAbsolute(ResBuildUp.SLASHING, -1);
        assertTrue(p.isResBuildUpImmune(ResBuildUp.SLASHING));
        // modifying while immune should have no effect
        p.modifyResBuildUp(ResBuildUp.SLASHING, 50);
        assertEquals(-1, p.getResBuildUp(ResBuildUp.SLASHING));

        // absolute set clamp behavior ( >100 -> 100, <0 and not -1 -> 0 )
        p.setResBuildUpAbsolute(ResBuildUp.WATER, 150);
        assertEquals(100, p.getResBuildUp(ResBuildUp.WATER));
        p.setResBuildUpAbsolute(ResBuildUp.WATER, -5);
        assertEquals(0, p.getResBuildUp(ResBuildUp.WATER));
    }

    @Test
    public void decayRespectsResistanceAndImmunity() {
        Player p = new Player();
        // set initial buildups
        p.setResBuildUpAbsolute(ResBuildUp.FIRE, 100);
        p.setResBuildUpAbsolute(ResBuildUp.WATER, 100);
        p.setResBuildUpAbsolute(ResBuildUp.SLASHING, -1); // immune

        // set resistances: FIRE=100 -> decay = floor(10*(100/100)) = 10
        // WATER=50 -> decay = floor(10*(100/50)) = 20 with current formula
        p.setResistance(Resistances.FIRE, 100);
        p.setResistance(Resistances.WATER, 50);

        ResistanceUtil.decayResBuildUps(p);

    assertEquals(90, p.getResBuildUp(ResBuildUp.FIRE), "FIRE should decay by 10");
    assertEquals(85, p.getResBuildUp(ResBuildUp.WATER), "WATER should decay by 15 (resistance 50)");
        assertEquals(-1, p.getResBuildUp(ResBuildUp.SLASHING), "Immune buildups should not decay");
    }

    @Test
    public void propertyAppliesNumericAndImmunityModifiers() {
        Player p = new Player();
        // base values
        assertEquals(0, p.getResBuildUp(ResBuildUp.FIRE));

        // Create a property that adds +30 fire buildup and grants slashing immunity
        String json = "{\"id\":9001,\"name\":\"TestProp\",\"type\":\"BUFF\",\"resBuildUp\":{\"FIRE\":30,\"SLASHING\":-1}}";
        Gson gson = new Gson();
        Property prop = gson.fromJson(json, Property.class);

        // apply property
        prop.onApply(p);
        assertEquals(30, p.getResBuildUp(ResBuildUp.FIRE), "Property should add +30 FIRE buildup");
        assertEquals(-1, p.getResBuildUp(ResBuildUp.SLASHING), "Property should set SLASHING to immune");

        // remove property should revert
        prop.onRemove(p);
        assertEquals(0, p.getResBuildUp(ResBuildUp.FIRE), "Removing property should revert numeric delta");
        assertEquals(0, p.getResBuildUp(ResBuildUp.SLASHING), "Removing property should restore previous value (0)");
    }

    @Test
    public void addBuildUpAndFreshSkipDecay() {
        Player p = new Player();
        // Set resistance to 100% so amount = floor(20 * 1.0 * 1.0) = 20
        p.setResistance(Resistances.FIRE, 100);
        ResistanceUtil.addBuildUp(p, Resistances.FIRE, 1.0f);
        assertEquals(20, p.getResBuildUp(ResBuildUp.FIRE), "addBuildUp should apply BASE_BUILD_UP * mult * resistanceFactor");

        // First decay should skip because the buildup was just added
        ResistanceUtil.decayResBuildUps(p);
        assertEquals(20, p.getResBuildUp(ResBuildUp.FIRE), "fresh buildup should skip decay once");

        // Second decay should apply: decay for resistance 100 is (200-100)/10 = 10
        ResistanceUtil.decayResBuildUps(p);
        assertEquals(10, p.getResBuildUp(ResBuildUp.FIRE), "after skip, next decay should reduce by expected amount");
    }
}
