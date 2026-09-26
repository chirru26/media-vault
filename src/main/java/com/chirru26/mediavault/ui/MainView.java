package com.chirru26.mediavault.ui;

import com.chirru26.mediavault.data.CollectionRepository;
import com.chirru26.mediavault.data.MediaRepository;
import com.chirru26.mediavault.data.SettingsRepository;
import com.chirru26.mediavault.model.MediaItem;
import com.chirru26.mediavault.service.MediaFilter;
import com.chirru26.mediavault.service.MediaScanner;
import com.chirru26.mediavault.media.ThumbnailService;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.awt.Desktop;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class MainView {
    private final MediaScanner scanner;
    private final MediaRepository repository;
    private final CollectionRepository collections = new CollectionRepository();
    private final SettingsRepository settings = new SettingsRepository();
    private final MediaFilter filter = new MediaFilter();
    private final ThumbnailService thumbnails = new ThumbnailService();
    private final GridPane grid = new GridPane();
    private final Label status = new Label("Ready");
    private final Label selectedName = new Label("Nothing selected");
    private final Label selectedDetails = new Label("Select an item to see metadata and actions.");
    private final TextField search = new TextField();
    private final ComboBox<String> type = new ComboBox<>();
    private final VBox sidebar = new VBox(8);
    private final VBox content = new VBox(18);
    private List<MediaItem> all = List.of();
    private MediaItem selected;
    private String currentView = "All Media";

    public MainView(MediaScanner scanner, MediaRepository repository) { this.scanner = scanner; this.repository = repository; }

    public BorderPane build() {
        var root = new BorderPane();
        root.setStyle("-fx-background-color:#f4f6f8;-fx-font-family:'Segoe UI';");
        root.setTop(header(root)); root.setLeft(sidebar()); root.setCenter(content); root.setRight(detailsPanel());
        refresh();
        return root;
    }

    private Node header(BorderPane root) {
        var brand = new Label("MediaVault"); brand.setStyle("-fx-font-size:24px;-fx-font-weight:bold;");
        search.setPromptText("Search files, folders, extensions..."); search.textProperty().addListener((o,a,b)->renderCurrent());
        type.getItems().addAll("All","IMAGE","VIDEO","AUDIO","DOCUMENT","OTHER"); type.setValue("All"); type.setOnAction(e->renderCurrent());
        var importFolder = new Button("+ Import Folder"); importFolder.setOnAction(e->chooseFolder(importFolder));
        var importFiles = new Button("Import Files"); importFiles.setOnAction(e->importFiles(importFiles));
        var refresh = new Button("↻"); refresh.setTooltip(new Tooltip("Refresh library")); refresh.setOnAction(e->refresh());
        var row = new HBox(12,brand,search,type,importFolder,importFiles,refresh); row.setAlignment(Pos.CENTER_LEFT); row.setPadding(new Insets(16,22,16,22));
        HBox.setHgrow(search,Priority.ALWAYS); row.setStyle("-fx-background-color:white;-fx-border-color:#e1e5ea;-fx-border-width:0 0 1 0;");
        return row;
    }

    private Node sidebar() {
        sidebar.setPadding(new Insets(20,14,20,14)); sidebar.setPrefWidth(205); sidebar.setStyle("-fx-background-color:white;-fx-border-color:#e1e5ea;-fx-border-width:0 1 0 0;");
        var library = new Label("LIBRARY"); library.setStyle("-fx-font-size:11px;-fx-font-weight:bold;-fx-text-fill:#7b8490;");
        sidebar.getChildren().setAll(library);
        for (String name : new String[]{"All Media","Photos","Videos","Audio","Documents","Favorites","Duplicates","Trash"}) sidebar.getChildren().add(nav(name));
        sidebar.getChildren().add(new Separator());
        var organization = new Label("ORGANIZE"); organization.setStyle("-fx-font-size:11px;-fx-font-weight:bold;-fx-text-fill:#7b8490;"); sidebar.getChildren().add(organization);
        try { for (String a : collections.albums()) sidebar.getChildren().add(nav("Album: "+a)); } catch(Exception ignored) {}
        try { for (String t : collections.tags()) sidebar.getChildren().add(nav("Tag: "+t)); } catch(Exception ignored) {}
        sidebar.getChildren().add(new Separator()); sidebar.getChildren().add(nav("Settings"));
        return sidebar;
    }

    private Button nav(String text) {
        var b=new Button(text); b.setMaxWidth(Double.MAX_VALUE); b.setAlignment(Pos.CENTER_LEFT); b.setPadding(new Insets(9,11,9,11));
        b.setStyle(text.equals(currentView)?"-fx-background-color:#e9eefc;-fx-font-weight:bold;":"-fx-background-color:transparent;");
        b.setOnAction(e->{currentView=text; rebuildSidebar(); renderCurrent();}); return b;
    }
    private void rebuildSidebar(){sidebar().setVisible(true);}

    private Node detailsPanel() {
        var panel=new VBox(12); panel.setPrefWidth(305); panel.setPadding(new Insets(22)); panel.setStyle("-fx-background-color:white;-fx-border-color:#e1e5ea;-fx-border-width:0 0 0 1;");
        var title=new Label("Details"); title.setStyle("-fx-font-size:18px;-fx-font-weight:bold;"); selectedName.setWrapText(true); selectedName.setStyle("-fx-font-weight:bold;-fx-font-size:15px;"); selectedDetails.setWrapText(true); selectedDetails.setStyle("-fx-text-fill:#65707c;-fx-font-size:12px;");
        var viewer=new Button("Open Viewer"); viewer.setMaxWidth(Double.MAX_VALUE); viewer.setOnAction(e->openViewer());
        var favorite=new Button("Toggle Favorite"); favorite.setMaxWidth(Double.MAX_VALUE); favorite.setOnAction(e->toggleFavorite());
        var rename=new Button("Rename"); rename.setMaxWidth(Double.MAX_VALUE); rename.setOnAction(e->renameSelected());
        var move=new Button("Move"); move.setMaxWidth(Double.MAX_VALUE); move.setOnAction(e->moveSelected());
        var reveal=new Button("Show in File Manager"); reveal.setMaxWidth(Double.MAX_VALUE); reveal.setOnAction(e->revealSelected());
        var trash=new Button("Move to Trash"); trash.setMaxWidth(Double.MAX_VALUE); trash.setOnAction(e->trashSelected());
        var tag=new Button("Add Tag"); tag.setMaxWidth(Double.MAX_VALUE); tag.setOnAction(e->tagSelected());
        panel.getChildren().addAll(title,new Separator(),selectedName,selectedDetails,viewer,favorite,rename,move,reveal,trash,tag); return panel;
    }

    private void libraryShell(String title,String subtitle){
        content.getChildren().clear(); var h=new VBox(3); var t=new Label(title); t.setStyle("-fx-font-size:22px;-fx-font-weight:bold;"); var s=new Label(subtitle); s.setStyle("-fx-text-fill:#6d7680;"); h.getChildren().addAll(t,s); grid.setHgap(15);grid.setVgap(15); content.setPadding(new Insets(24)); content.getChildren().addAll(h,status,grid);
    }

    private void renderCurrent(){
        if(currentView.equals("Settings")){showSettings();return;}
        try{
            List<MediaItem> source=switchView();
            var filtered=filter.apply(source,search.getText(),type.getValue());
            libraryShell(currentView,currentView.startsWith("Album:")||currentView.startsWith("Tag:")?"Organized collection":"Your local media library");
            status.setText(filtered.size()+" items"); grid.getChildren().clear();
            if(filtered.isEmpty()){grid.add(new Label("Nothing to show here."),0,0);return;}
            for(int i=0;i<filtered.size();i++)grid.add(card(filtered.get(i)),i%5,i/5);
        }catch(Exception e){libraryShell("MediaVault","Error");grid.add(new Label("Unable to load library: "+e.getMessage()),0,0);}
    }

    private List<MediaItem> switchView() throws Exception {
        return switch(currentView){
            case "Favorites" -> repository.findFavorites(); case "Trash" -> repository.findTrash(); case "Duplicates" -> repository.findDuplicates();
            case "Photos" -> repository.findAll().stream().filter(x->x.mediaType().name().equals("IMAGE")).toList();
            case "Videos" -> repository.findAll().stream().filter(x->x.mediaType().name().equals("VIDEO")).toList();
            case "Audio" -> repository.findAll().stream().filter(x->x.mediaType().name().equals("AUDIO")).toList();
            case "Documents" -> repository.findAll().stream().filter(x->x.mediaType().name().equals("DOCUMENT")).toList();
            default -> { if(currentView.startsWith("Album:")){long id=collections.albumId(currentView.substring(6));yield id<0?List.of():repository.findByAlbum(id);} if(currentView.startsWith("Tag:")){yield repository.findByTag(currentView.substring(4));} yield repository.findAll(); }
        };
    }

    private Node card(MediaItem item){
        var box=new VBox(7); box.setPrefWidth(175); box.setPadding(new Insets(6));
        var preview=new StackPane(); preview.setPrefSize(175,115); preview.setStyle("-fx-background-color:#e4e7eb;-fx-background-radius:10;");
        if(item.mediaType().name().equals("IMAGE")&&Files.isRegularFile(item.filePath())){var image=thumbnails.load(item.filePath());if(image!=null){var v=new ImageView(image);v.setFitWidth(175);v.setFitHeight(115);v.setPreserveRatio(true);preview.getChildren().add(v);}}
        if(preview.getChildren().isEmpty()){var icon=new Label(switch(item.mediaType()){case IMAGE->"🖼";case VIDEO->"▶";case AUDIO->"♫";case DOCUMENT->"▤";case OTHER->"•";});icon.setStyle("-fx-font-size:30px;");preview.getChildren().add(icon);}
        var name=new Label(item.fileName()); name.setMaxWidth(175); name.setEllipsisString("...");
        var meta=new Label(formatSize(item.fileSize()));meta.setStyle("-fx-text-fill:#8a939d;-fx-font-size:11px;"); box.getChildren().addAll(preview,name,meta);
        box.setOnMouseClicked(e->{select(item);if(e.getClickCount()==2)openViewer();}); box.setStyle("-fx-cursor:hand;-fx-background-color:white;-fx-background-radius:10;"); return box;
    }

    private void select(MediaItem item){selected=item;selectedName.setText(item.fileName());try{selectedDetails.setText("Type: "+item.mediaType()+"\nSize: "+formatSize(item.fileSize())+"\nModified: "+item.modifiedAt()+"\nDimensions: "+(item.width()==null?"—":item.width()+" × "+item.height())+"\nHash: "+(item.contentHash()==null?"—":item.contentHash())+"\n\nLocation:\n"+item.filePath()+"\n\nTags: "+String.join(", ",collections.tagsForMedia(item.id()))+"\nAlbums: "+String.join(", ",collections.albumsForMedia(item.id())));}catch(Exception e){selectedDetails.setText(item.filePath().toString());}}

    private void chooseFolder(Button source){var chooser=new DirectoryChooser();chooser.setTitle("Import Media Folder");var folder=chooser.showDialog(source.getScene().getWindow());if(folder==null)return;scanAsync(folder.toPath(),source);}
    private void importFiles(Button source){var chooser=new javafx.stage.FileChooser();chooser.setTitle("Import Media Files");chooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("Media and documents","*.jpg","*.jpeg","*.png","*.gif","*.webp","*.mp4","*.mkv","*.mov","*.avi","*.mp3","*.wav","*.flac","*.pdf","*.docx","*.txt"));var files=chooser.showOpenMultipleDialog(source.getScene().getWindow());if(files==null||files.isEmpty())return;scanAsync(files.stream().map(java.io.File::toPath).toList(),source);}
    private void scanAsync(Path folder,Button source){scanAsync(List.of(folder),source);}
    private void scanAsync(List<Path> paths,Button source){source.setDisable(true);status.setText("Indexing...");new Thread(()->{try{for(Path p:paths)scanner.scan(p);Platform.runLater(()->{source.setDisable(false);rebuildSidebar();refresh();});}catch(Exception e){Platform.runLater(()->{source.setDisable(false);status.setText("Index failed: "+e.getMessage());});}},"mediavault-indexer").start();}
    private void refresh(){try{all=repository.findAll();rebuildSidebar();renderCurrent();}catch(Exception e){status.setText("Database error: "+e.getMessage());}}

    private void openViewer(){if(selected==null)return;new MediaViewer().show(selected,(Stage)content.getScene().getWindow());}
    private void toggleFavorite(){if(selected==null)return;try{repository.setFavorite(selected.id(),!repository.findFavorites().stream().anyMatch(x->x.id()==selected.id()));refresh();}catch(Exception e){showError(e);}}
    private void trashSelected(){if(selected==null)return;try{repository.setTrashed(selected.id(),true);selected=null;refresh();}catch(Exception e){showError(e);}}
    private void renameSelected(){if(selected==null)return;var dialog=new TextInputDialog(selected.fileName());dialog.setTitle("Rename Media");dialog.setHeaderText("Rename file");dialog.setContentText("New name:");dialog.showAndWait().ifPresent(name->{try{Path target=selected.filePath().resolveSibling(name.trim());if(name.isBlank()||Files.exists(target)){throw new IllegalArgumentException("Name is empty or already exists");}Files.move(selected.filePath(),target);repository.updatePath(selected.id(),target);refresh();}catch(Exception e){showError(e);}});}
    private void moveSelected(){if(selected==null)return;var chooser=new DirectoryChooser();chooser.setTitle("Move Media");var dir=chooser.showDialog(content.getScene().getWindow());if(dir==null)return;try{Path target=dir.toPath().resolve(selected.fileName());if(Files.exists(target))throw new IllegalArgumentException("A file with this name already exists there");Files.move(selected.filePath(),target);repository.updatePath(selected.id(),target);refresh();}catch(Exception e){showError(e);}}
    private void revealSelected(){if(selected==null)return;try{if(Desktop.isDesktopSupported())Desktop.getDesktop().open(selected.filePath().getParent().toFile());}catch(Exception e){showError(e);}}
    private void tagSelected(){if(selected==null)return;var dialog=new TextInputDialog();dialog.setTitle("Tag Media");dialog.setHeaderText("Add a tag to "+selected.fileName());dialog.setContentText("Tag:");dialog.showAndWait().ifPresent(tag->{try{collections.tagMedia(selected.id(),tag);select(selected);}catch(Exception e){showError(e);}});}

    private void showSettings(){libraryShell("Settings","Application preferences and maintenance");grid.getChildren().clear();var box=new VBox(12);box.setPadding(new Insets(12));var db=new Label("Database: "+Path.of(System.getProperty("user.home"),".mediavault","mediavault.db"));var theme=new CheckBox("Use compact cards");try{theme.setSelected(Boolean.parseBoolean(settings.get("compact_cards","false")));}catch(Exception ignored){}theme.setOnAction(e->{try{settings.set("compact_cards",Boolean.toString(theme.isSelected()));}catch(Exception ex){showError(ex);}});var album=new Button("Create Album");album.setOnAction(e->createAlbum());var tag=new Button("Create Tag");tag.setOnAction(e->createTag());var clearTrash=new Button("Empty Trash");clearTrash.setOnAction(e->emptyTrash());box.getChildren().addAll(new Label("Storage"),db,theme,new Separator(),album,tag,clearTrash);grid.add(box,0,0);}
    private void createAlbum(){var d=new TextInputDialog();d.setTitle("Create Album");d.setContentText("Album name:");d.showAndWait().ifPresent(n->{try{collections.createAlbum(n);rebuildSidebar();}catch(Exception e){showError(e);}});}
    private void createTag(){var d=new TextInputDialog();d.setTitle("Create Tag");d.setContentText("Tag name:");d.showAndWait().ifPresent(n->{try{collections.addTag(n);rebuildSidebar();}catch(Exception e){showError(e);}});}
    private void emptyTrash(){try{var trash=repository.findTrash();for(var item:trash){try{Files.deleteIfExists(item.filePath());}catch(Exception ignored){}repository.deletePermanently(item.id());}refresh();}catch(Exception e){showError(e);}}
    private void showError(Exception e){new Alert(Alert.AlertType.ERROR,"Operation failed: "+e.getMessage(),ButtonType.OK).showAndWait();}
    private String formatSize(long bytes){if(bytes<1024)return bytes+" B";double v=bytes;String[] u={"KB","MB","GB","TB"};int i=-1;do{v/=1024;i++;}while(v>=1024&&i<u.length-1);return String.format("%.2f %s",v,u[i]);}
}
