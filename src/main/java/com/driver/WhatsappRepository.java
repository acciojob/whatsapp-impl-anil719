package com.driver;

import java.util.*;

import org.springframework.stereotype.Repository;

@Repository
public class WhatsappRepository {

    //Assume that each user belongs to at most one group
    //You can use the below mentioned hashmaps or delete these and create your own.
    private HashMap<Group, List<User>> groupUserMap;
    private HashMap<Group, List<Message>> groupMessageMap;
    private HashMap<Message, User> senderMap;
    private HashMap<Group, User> adminMap;
    private HashSet<String> userMobile;
    private int customGroupCount;
    private int messageId;

    public WhatsappRepository(){
        this.groupMessageMap = new HashMap<Group, List<Message>>();
        this.groupUserMap = new HashMap<Group, List<User>>();
        this.senderMap = new HashMap<Message, User>();
        this.adminMap = new HashMap<Group, User>();
        this.userMobile = new HashSet<>();
        this.customGroupCount = 0;
        this.messageId = 0;
    }

    public String createUser(String name, String mobile) throws Exception{
        if(userMobile.contains(mobile)) throw new Exception("User already exists");

        User user = new User(name, mobile);
        userMobile.add(mobile);
        return "SUCCESS" ;
    }
    public Group createGroup(List<User> users){
        if(users.size() == 2){
            Group group = new Group(users.get(1).getName(),2);
            groupUserMap.put(group, users);
            groupMessageMap.put(group, new ArrayList<>());
            return group;
        }
        else {
            customGroupCount++;
            int usersCount = users.size();
            Group group = new Group("Group "+customGroupCount, usersCount);
            groupUserMap.put(group, users);
            groupMessageMap.put(group, new ArrayList<>());
            User admin = users.get(0);          //first user is admin
            adminMap.put(group, admin);
            return group;
        }
    }

    public int createMessage(String content){
        this.messageId++;
        Message message = new Message(messageId, content);
        return message.getId();
    }

    public int sendMessage(Message message, User user, Group group) throws Exception{
        if(!groupUserMap.containsKey(group)) throw new Exception("Group does not exist");

        HashSet<User> groupMembers = new HashSet<>(groupUserMap.get(group));    //get All Users
        if(!groupMembers.contains(user)) throw new Exception("You are not allowed to send message");

        List<Message> messageList = groupMessageMap.get(group);
        messageList.add(message);
        groupMessageMap.put(group, messageList);
        return messageList.size();
    }

    public String changeAdmin(User approver, User user, Group group)throws Exception {
        if (!groupUserMap.containsKey(group)) throw new Exception("Group does not exist");
        //get current admin first
        User admin = groupUserMap.get(group).get(0);    //from the list of users 1st was the admin

        //approver is not the admin of current group
        if(!admin.getName().equals(approver.getName())) throw new Exception("Approver does not have rights");

        //checking that the user is member in our group
        if(!groupUserMap.get(group).contains(user)) throw new Exception("User is not a participant");

        adminMap.put(group,admin);
        return "SUCCESS";
    }
    public int removeUser(User user) throws Exception{
        //getting all groups first and then checking user is there arent

        //A user belongs to exactly one group
        //If user is not found in any group, throw "User not found" exception
        //If user is found in a group and it is the admin, throw "Cannot remove admin" exception
        //If user is not the admin, remove the user from the group, remove all its messages from all the databases, and update relevant attributes accordingly.
        //If user is removed successfully, return (the updated number of users in the group + the updated number of messages in group + the updated number of overall messages)
        for(Group grp : groupUserMap.keySet()){
            List<User> userList = groupUserMap.get(grp);
            if(userList.contains(user)){
               for(User admin : adminMap.values()){
                   if(user == admin) throw new Exception("Cannot remove admin");
               }
               userList.remove(user);
               for(Message msg : senderMap.keySet()){
                   if(user == senderMap.get(msg)) senderMap.remove(msg);
                   groupMessageMap.get(grp).remove(msg);
                   return (groupUserMap.get(grp).size() + groupMessageMap.get(grp).size() + senderMap.size());
               }
            }
        }
        throw new Exception("User not found");
    }

    public String findMessage(Date start, Date end, int k) throws Exception{
        // Find the Kth latest message between start and end (excluding start and end)
        // If the number of messages between given time is less than K, throw "K is greater than the number of messages" exception
        TreeMap<Integer, String> msgMp = new TreeMap<>();
        ArrayList<Integer> al = new ArrayList<>();
        for(Message msg : senderMap.keySet()){
            if(msg.getTimestamp().after(start) && msg.getTimestamp().before(end)){
                msgMp.put(msg.getId(), msg.getContent());
                al.add(msg.getId());
            }
        }
        if(msgMp.size() < k) throw new Exception("K is greater than the number of messages") ;
        Collections.sort(al);
        int K = al.get(al.size()-k);
        return msgMp.get(K);
    }
}
