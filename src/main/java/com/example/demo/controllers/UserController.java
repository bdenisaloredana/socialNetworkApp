package com.example.demo.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import com.example.demo.dto.FriendshipDto;
import com.example.demo.dto.MessageDto;
import com.example.demo.entities.Friendship;
import com.example.demo.entities.Status;
import com.example.demo.entities.User;
import com.example.demo.services.FriendshipService;
import com.example.demo.services.MessageService;
import com.example.demo.services.UserService;
import com.example.demo.utils.observer.Observer;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class UserController implements Observer {

    public TableView<User> friendsTableView;
    public TableColumn<User, String> friendsTableViewFirstName;
    public TableColumn<User, String> friendsTableViewLastName;
    public Button friendsTableViewDeleteButton;
    public TableView<FriendshipDto> requestsTableView;
    public TableColumn<FriendshipDto, String> requestsTableViewFirstName;
    public TableColumn<FriendshipDto, String> requestsTableViewLastName;
    public TableColumn<FriendshipDto, String> requestsTableViewStatus;
    public TableColumn<FriendshipDto, String> requestsTableViewDate;
    public TableView<User> searchTableView;
    public TableColumn<User, String> searchTableViewFirstName;
    public TableColumn<User, String> searchTableViewLastName;
    public Button searchTableViewAddButton;
    public Button requestsTableViewAcceptButton;
    public Button requestsTableViewDeclineButton;
    public TextField searchTableViewTextField;
    public TableView<MessageDto> messagesTableView;
    public TableColumn<MessageDto, String> messagesTableViewUsernameColumn;
    public TableColumn<MessageDto, String> messagesTableViewMessagesColumn;
    public Button openConversationButton;
    public TextField messageTextField;
    public TableView<User> friendsTableViewMessages;
    public TableColumn<User, String> friendsTableViewUsername;
    public Button sendMessageButton;
    public TableView<FriendshipDto> sentRequestsTableView;
    public TableColumn<FriendshipDto, String> sentRequestsTableViewFirstNameTableColumn;
    public TableColumn<FriendshipDto, String> sentRequestsTableViewLastNameTableColumn;

    ObservableList<User> modelFriends = FXCollections.observableArrayList();
    ObservableList<User> searchedUsersModel = FXCollections.observableArrayList();
    ObservableList<FriendshipDto> modelRequestsSent = FXCollections.observableArrayList();
    ObservableList<FriendshipDto> modelRequests = FXCollections.observableArrayList();
    ObservableList<User> modelSearch= FXCollections.observableArrayList();
    ObservableList<MessageDto> modelMessages= FXCollections.observableArrayList();
    UserService userService;
    FriendshipService friendshipService;
    MessageService messageService;
    User user;
    User friend;

    /***
     * Sets up the UserController.
     * @param userService the users service
     * @param friendshipService the friendships service
     * @param messageService the messages service
     * @param user the current active user
     */
    public void setUserService(UserService userService, FriendshipService friendshipService, MessageService messageService,User user){
        this.userService = userService;
        this.friendshipService = friendshipService;
        this.user =user;
        this.messageService = messageService;
        userService.addObserver(this);
        messageService.addObserver(this);
        friendshipService.addObserver(this);
        initModelFriendships();
        initModelRequests();
        innitSentRequests();
        innitModelSearch();


    }

    /***
     * Sets the values for each column of the table view.
     */
    @FXML
    public void initialize() {
        friendsTableViewFirstName.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        friendsTableViewLastName.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        friendsTableView.setItems(modelFriends);

        requestsTableViewFirstName.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        requestsTableViewLastName.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        requestsTableViewStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        requestsTableViewDate.setCellValueFactory(new PropertyValueFactory<>("requestDate"));
        requestsTableView.setItems(modelRequests);

        searchTableViewFirstName.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        searchTableViewLastName.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        searchTableView.setItems(searchedUsersModel);

        messagesTableViewUsernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        messagesTableViewMessagesColumn.setCellValueFactory(new PropertyValueFactory<>("text"));
        messagesTableView.setItems(modelMessages);

        friendsTableViewUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        friendsTableViewMessages.setItems(modelFriends);

        sentRequestsTableViewFirstNameTableColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        sentRequestsTableViewLastNameTableColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        sentRequestsTableView.setItems(modelRequestsSent);

        messageTextField.setPromptText("Write message...");
        searchTableViewTextField.textProperty().addListener((observable, oldValue, newValue)->{
            searchedUsersModel.setAll(modelSearch);
            searchedUsersModel.setAll(searchedUsersModel.stream().filter(user->user.getFirstName().contains(newValue.trim()) ||
                    user.getLastName().equals(newValue.trim())).collect(Collectors.toList()));

        });
        searchTableViewTextField.setPromptText("Search user...");
    }

    /***
     * Fills the modelRequestsSent with information.
     */
    private void innitSentRequests() {
        try {
            List<FriendshipDto> sent = friendshipService.findRequestsSentByUser(this.user);
            modelRequestsSent.setAll(sent);
        }catch (SQLException sqlException){
            System.out.println(sqlException.getMessage());
        }
    }

    /***
     * Fills the modelMessages with information.
     * @param friend the friend the user is messaging
     */
    private void innitMessages(User friend){
        List<MessageDto> messages = messageService.getMessagesBetweenFriends(user, friend);
        modelMessages.setAll(messages);
    }

    /***
     * Fills the modelFriends with information.
     */
    private void initModelFriendships() {
        try {
            List<Long> friendsid = this.friendshipService.findFriendsOfUser(user);
            List<User> users = new ArrayList<>();
            for (Long id : friendsid)
                users.add(userService.findUser(id));
            modelFriends.setAll(users);
        }catch (SQLException sqlException){
            System.out.println(sqlException.getMessage());
        }

    }

    /***
     * Fills the modelSearch and searchedUsersModel with information.
     */
    public void innitModelSearch(){
        List<User> users = userService.getAll();
        users.remove(user);
        searchedUsersModel.setAll(users);
        modelSearch.setAll(searchedUsersModel);
    }

    /***
     *Fills the modelRequests with information.
     */
    public void initModelRequests(){
        try {
            List<FriendshipDto> friendships = friendshipService.findFriendshipsOfUser(this.user);
            modelRequests.setAll(friendships);
        }catch (SQLException sqlException){
            System.out.println(sqlException.getMessage());
        }

    }

    /***
     * Handles the removing of a friend.
     */
    public void handleDeleteFriend(){
        User friend = friendsTableView.getSelectionModel().getSelectedItem();
        Long idFriend = friend.getId();
        try {
            if (friend != null) {
                Friendship friendship = friendshipService.findFriendshipByReceiverAndSender(idFriend, user.getId());
                if (friendship != null) {
                    friendshipService.deleteFriendship(friendship.getId());
                    MessageAlert.showMessage(null, Alert.AlertType.CONFIRMATION, "Confirmation", "Friend removed!");
                } else {
                    friendship = friendshipService.findFriendshipByReceiverAndSender(user.getId(), idFriend);
                    if (friendship != null) {
                        friendshipService.deleteFriendship(friendship.getId());
                        MessageAlert.showMessage(null, Alert.AlertType.CONFIRMATION, "Confirmation", "Friend removed!");
                    }
                }
            }
        }catch (SQLException sqlException){
            System.out.println(sqlException.getMessage());
        }

    }

    /***
     * Handles the accepting of a friend request.
     */
    public void handleAcceptRequest() {
        FriendshipDto friend = requestsTableView.getSelectionModel().getSelectedItem();
        try {
            if (friend != null) {
                if (Status.valueOf(friend.getStatus()) == Status.Pending) {
                    friendshipService.updateFriendship(friend.getIdFriendship(), Status.Accepted);

                } else MessageAlert.showErrorMessage(null, "The status of the friendship has already been set!");

            }
        }catch (SQLException sqlException){
            System.out.println(sqlException.getMessage());
        }
    }

    /***
     * Handles the declining of a friendship request.
     */
    public void handleDeclineRequest(){

        FriendshipDto friend = requestsTableView.getSelectionModel().getSelectedItem();
        try {
            if (friend != null) {
                if (Status.valueOf(friend.getStatus()) == Status.Pending) {
                    friendshipService.updateFriendship(friend.getIdFriendship(), Status.Declined);
                } else
                    MessageAlert.showErrorMessage(null, "The status of the friendship has already been set!");

            }
        }catch (SQLException sqlException){
            System.out.println(sqlException.getMessage());
        }
    }

    /***
     * Updates the models after a modification.
     */
    @Override
    public void update(){
        initModelFriendships();
        initModelRequests();
        if (friend != null)
            innitMessages(friend);
        innitSentRequests();


    }

    /***
     *Handles the sending of a friendship request.
     */
    public void addNewFriend() {
        User friendToAdd = searchTableView.getSelectionModel().getSelectedItem();

        try {
            List<FriendshipDto> friendships = friendshipService.findFriendshipsOfUser(this.user);
            List<Long> friends = friendshipService.findFriendsOfUser(user);
            boolean ok = false;
            for (Long id : friends)
                if (Objects.equals(id, friendToAdd.getId()))
                    ok = true;
            if (ok) {
                MessageAlert.showErrorMessage(null, "You are already friends with this person!");
            } else {


                ok = false;
                for (FriendshipDto friendshipDto : friendships)
                    if (Objects.equals(friendshipDto.getIdFriend(), friendToAdd.getId())) {
                        ok = true;
                        break;
                    }
                if (!ok) {
                    friendshipService.addFriendship(user.getId(), friendToAdd.getId());
                    MessageAlert.showMessage(null, Alert.AlertType.CONFIRMATION, "Confirmation", "Friendship request sent!");
                } else
                    MessageAlert.showErrorMessage(null, "You already have a friendship request from this person!");
            }
        }catch (SQLException sqlException){
            System.out.println(sqlException.getMessage());
        }

    }

    /***
     * Handles the opening of a conversation
     */
    public void handleOpenConversation() {
        User friend = friendsTableViewMessages.getSelectionModel().getSelectedItem();
        this.friend = friend;
        innitMessages(friend);
    }

    /***
     *  Handles the sending of a message.
     */
    public void handleSendMessage(){
        String message = messageTextField.getText();
        try {
            messageService.addMessage(user.getId(), friend.getId(), message);
        }catch (SQLException sqlException){
            System.out.println(sqlException.getMessage());
        }
    }

    /***
     * Handles the deleting of a sent friendship request.
     */
    public void handleDeleteSentRequest(){
        FriendshipDto request = sentRequestsTableView.getSelectionModel().getSelectedItem();
        try {
            friendshipService.deleteFriendship(request.getIdFriendship());
        }catch (SQLException sqlException){
            System.out.println(sqlException.getMessage());
        }
    }

}
