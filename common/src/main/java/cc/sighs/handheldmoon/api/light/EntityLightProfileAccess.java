package cc.sighs.handheldmoon.api.light;

/**
 * Mutable access contract for systems that want to control an entity light.
 * Target implementations are responsible for synchronising and persisting
 * the profile; common light calculation only consumes this contract.
 */
public interface EntityLightProfileAccess {
    /** Returns the effective profile for this entity. */
    EntityLightProfile getLightProfile();

    /** Replaces the effective profile and enables the override state. */
    void setLightProfile(EntityLightProfile profile);

    /** Restores the target's built-in profile derived from device state. */
    void clearLightProfileOverride();
}
