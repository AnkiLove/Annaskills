package dev.aurelium.auraskills.bukkit.trait;

import dev.aurelium.auraskills.bukkit.AuraSkills;
import dev.aurelium.auraskills.bukkit.util.AttributeCompat;
import dev.aurelium.auraskills.common.util.TestSession;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HpTraitTest {

    private ServerMock server;
    private AuraSkills plugin;
    private HpTrait hpTrait;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(AuraSkills.class, TestSession.create());
        hpTrait = new HpTrait(plugin);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void recognizesCurrentAndLegacyHealthModifierKeys() {
        AttributeModifier current = modifier(new NamespacedKey(plugin, "hp_trait"));
        AttributeModifier legacy = modifier(new NamespacedKey("auraskills", "hp_trait"));
        AttributeModifier unrelated = modifier(new NamespacedKey(plugin, "other_trait"));

        assertTrue(hpTrait.isSkillsHealthModifier(current));
        assertTrue(hpTrait.isSkillsHealthModifier(legacy));
        assertFalse(hpTrait.isSkillsHealthModifier(unrelated));
    }

    @Test
    void repeatedReloadNeverDuplicatesHealthModifier() {
        PlayerMock player = server.addPlayer();
        var user = plugin.getUser(player);

        assertDoesNotThrow(() -> {
            hpTrait.setHealth(player, user);
            hpTrait.setHealth(player, user);
        });

        AttributeInstance attribute = player.getAttribute(AttributeCompat.maxHealth);
        assertNotNull(attribute);
        long modifierCount = attribute.getModifiers().stream()
                .filter(hpTrait::isSkillsHealthModifier)
                .count();
        assertTrue(modifierCount <= 1);
    }

    private AttributeModifier modifier(NamespacedKey key) {
        return new AttributeModifier(key, 4.0, Operation.ADD_NUMBER, EquipmentSlotGroup.ANY);
    }
}
