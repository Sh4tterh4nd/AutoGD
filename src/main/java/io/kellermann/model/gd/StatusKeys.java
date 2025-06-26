package io.kellermann.model.gd;

public enum StatusKeys {
    DEFAULT("Unbekannter Status", 0.0, 0.0, GdTarget.VIDEO),
    VIDEO_IMAGE_DOWNLOAD("Video Intro Bild Download", 0.0, 0.02, GdTarget.VIDEO),
    VIDEO_IMAGE_TOVIDEO("Video Intro Bild zu Video", 0.02, 0.04, GdTarget.VIDEO),
    VIDEO_INTRO("Intro Video Generierung", 0.04, 0.10, GdTarget.VIDEO),
    VIDEO_CUT("Gottesdienst Video Schnitt", 0.10, 0.20, GdTarget.VIDEO),
    VIDEO_GENERATION("Gottesdienst Video Generierung", 0.20, 0.60, GdTarget.VIDEO),
    VIDEO_THUMBNAIL_PREPARE("Gottesdienst Thumbnail vorbereitung", 0.60, 0.70, GdTarget.VIDEO),
    VIDEO_THUMBNAIL_CREATE("Gottesdienst Thumbnail generierung", 0.70, 0.80, GdTarget.VIDEO),
    VIDEO_UPLOAD("Gottesdienst Video Youtube Upload", 0.80, 0.98, GdTarget.VIDEO),
    VIDEO_PLAYLIST("Gottesdienst zu Playlist hinzugefügt", 0.98, 0.99, GdTarget.VIDEO),
    VIDEO_REGISTER("Gottesdienst in GD Verwaltung registrieren", 0.99, 1, GdTarget.VIDEO);

    public final String title;
    public final double minProgress;
    public final double maxProgress;
    public final GdTarget gdTarget;

    private StatusKeys(String title, double minProgress, double maxProgress, GdTarget gdTarget) {
        this.title = title;
        this.minProgress = minProgress;
        this.maxProgress = maxProgress;
        this.gdTarget = gdTarget;
    }

}
