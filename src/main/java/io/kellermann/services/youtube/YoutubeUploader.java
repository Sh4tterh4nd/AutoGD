/*
 * Copyright (c) 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package io.kellermann.services.youtube;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.googleapis.media.MediaHttpUploaderProgressListener;
import com.google.api.client.http.InputStreamContent;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Playlist;
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.PlaylistItemSnippet;
import com.google.api.services.youtube.model.PlaylistSnippet;
import com.google.api.services.youtube.model.PlaylistStatus;
import com.google.api.services.youtube.model.ResourceId;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoSnippet;
import com.google.api.services.youtube.model.VideoStatus;
import io.kellermann.config.YoutubeConfiguration;
import io.kellermann.model.gdVerwaltung.WorshipMetaData;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

@Service
public class YoutubeUploader {
    private final String VIDEO_FILE_FORMAT = "video/*";

    private YoutubeConfiguration ytYoutubeConfiguration;

    private TemplatingEngine templatingEngine;

    private YouTube youTube;

    public YoutubeUploader(YouTube youtube, YoutubeConfiguration ytYoutubeConfiguration, TemplatingEngine templatingEngine) {
        this.ytYoutubeConfiguration = ytYoutubeConfiguration;
        this.templatingEngine = templatingEngine;
        this.youTube = youtube;
    }


    public void insertVideoToPlaylist(Video video, WorshipMetaData worshipMetaData) throws IOException {
        ResourceId resourceId = new ResourceId();
        resourceId.setKind("youtube#video");
        resourceId.setVideoId(video.getId());

        Playlist playlist = findOrCreatePlaylist(worshipMetaData.getSeries().getTitleLanguage(worshipMetaData.getServiceLanguage()));


        PlaylistItemSnippet playlistItemSnippet = new PlaylistItemSnippet();
        playlistItemSnippet.setTitle(worshipMetaData.getServiceTitle(worshipMetaData.getServiceLanguage()));
        playlistItemSnippet.setPlaylistId(playlist.getId());
        playlistItemSnippet.setResourceId(resourceId);

        PlaylistItem playlistItem = new PlaylistItem();
        playlistItem.setSnippet(playlistItemSnippet);

        YouTube.PlaylistItems.Insert playlistItemsInsertCommand =
                youTube.playlistItems().insert("snippet,contentDetails", playlistItem);
        PlaylistItem returnedPlaylistItem = playlistItemsInsertCommand.execute();
    }


    public Playlist findOrCreatePlaylist(String title) throws IOException {
        List<Playlist> items = youTube.playlists().list("id,contentDetails,snippet").setMine(true).execute().getItems();
        return items.stream().filter(s -> s.getSnippet().getTitle().equals(title)).findFirst().orElseGet(() -> createPlaylist(title));

    }

    public Playlist createPlaylist(String title) {
        try {
            PlaylistSnippet playlistSnippet = new PlaylistSnippet();
            playlistSnippet.setTitle(title);
            PlaylistStatus playlistStatus = new PlaylistStatus();
            playlistStatus.setPrivacyStatus("public");

            Playlist youTubePlaylist = new Playlist();
            youTubePlaylist.setSnippet(playlistSnippet);
            youTubePlaylist.setStatus(playlistStatus);
            YouTube.Playlists.Insert playlistInsertCommand = youTube.playlists().insert("snippet,status", youTubePlaylist);
            return playlistInsertCommand.execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public String uploadToYoutube(Path videoPath, WorshipMetaData worshipMetaData) {

        try {

            // Add extra information to the video before uploading.
            Video videoObjectDefiningMetadata = new Video();

            // Set the video to be publicly visible. This is the default
            // setting. Other supporting settings are "unlisted" and "private."
            VideoStatus status = new VideoStatus();
            status.setPrivacyStatus("public");
            status.setSelfDeclaredMadeForKids(false);

            videoObjectDefiningMetadata.setStatus(status);

            // Most of the video's metadata is set on the VideoSnippet object.
            VideoSnippet snippet = new VideoSnippet();

            snippet.setTitle(templatingEngine.processTemplateWithKeyModel(ytYoutubeConfiguration.getTitle(), worshipMetaData));
            snippet.setDescription(templatingEngine.processTemplateWithKeyModel(ytYoutubeConfiguration.getDescription(), worshipMetaData));


            snippet.setTags(ytYoutubeConfiguration.getTags());


            // Add the completed snippet object to the video resource.
            videoObjectDefiningMetadata.setSnippet(snippet);


            InputStreamContent mediaContent = new InputStreamContent(VIDEO_FILE_FORMAT, new FileInputStream(videoPath.toFile()));
            mediaContent.setLength(videoPath.toFile().length());

            YouTube.Videos.Insert videoInsert = youTube.videos()
                    .insert("snippet,statistics,status", videoObjectDefiningMetadata, mediaContent);


            MediaHttpUploader uploader = videoInsert.getMediaHttpUploader();
            uploader.setDirectUploadEnabled(false);

            MediaHttpUploaderProgressListener progressListener = new MediaHttpUploaderProgressListener() {
                public void progressChanged(MediaHttpUploader uploader) throws IOException {
                    switch (uploader.getUploadState()) {
                        case INITIATION_STARTED:
                            System.out.println("Initiation Started");
                            break;
                        case INITIATION_COMPLETE:
                            System.out.println("Initiation Completed");
                            break;
                        case MEDIA_IN_PROGRESS:
                            System.out.println("Upload percentage: " + uploader.getProgress());
                            break;
                        case MEDIA_COMPLETE:
                            System.out.println("Upload Completed!");
                            break;
                        case NOT_STARTED:
                            System.out.println("Upload Not Started!");
                            break;
                    }
                }
            };
            uploader.setProgressListener(progressListener);

            // Call the API and upload the video.
            Video returnedVideo = videoInsert.execute();

            if (Objects.nonNull(worshipMetaData.getSeries().getId())) {
                insertVideoToPlaylist(returnedVideo, worshipMetaData);
            }


            // Print data about the newly inserted video from the API response.
            System.out.println("\n================== Returned Video ==================\n");
            System.out.println("  - Id: " + returnedVideo.getId());
            System.out.println("  - Title: " + returnedVideo.getSnippet().getTitle());
            System.out.println("  - Tags: " + returnedVideo.getSnippet().getTags());
            System.out.println("  - Privacy Status: " + returnedVideo.getStatus().getPrivacyStatus());
            System.out.println("  - Video Count: " + returnedVideo.getStatistics().getViewCount());

            return "https://youtu.be/" + returnedVideo.getId();
        } catch (GoogleJsonResponseException e) {
            System.err.println("GoogleJsonResponseException code: " + e.getDetails().getCode() + " : "
                    + e.getDetails().getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("IOException: " + e.getMessage());
            e.printStackTrace();
        } catch (Throwable t) {
            System.err.println("Throwable: " + t.getMessage());
            t.printStackTrace();
        }
        return "";
    }

}
