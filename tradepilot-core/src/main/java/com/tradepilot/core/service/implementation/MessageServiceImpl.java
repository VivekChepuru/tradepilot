package com.tradepilot.core.service.implementation;

import com.tradepilot.core.entity.Chat;
import com.tradepilot.core.entity.Message;
import com.tradepilot.core.entity.User;
import com.tradepilot.core.repository.ChatRepository;
import com.tradepilot.core.repository.MessageRepository;
import com.tradepilot.core.repository.UserRepository;
import com.tradepilot.core.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class MessageServiceImpl implements MessageService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChatRepository chatRepository;

    @Override
    public Message sendMessage(UUID authenticatedUserId, UUID chatId, String content) {
        //Validate user exists
        User sender = userRepository.findById(authenticatedUserId)
                .orElseThrow(()-> new IllegalArgumentException("Sender not found"));
        //Validate chat exists
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(()-> new IllegalArgumentException("Chat not found"));

        //Verify sender is a participant of the chat
        if (!chat.getParticipants().contains(sender)) {
            throw new IllegalArgumentException("You are not a participant of this chat");
        }
        Message message = new Message();
        message.setChat(chat);
        message.setSender(sender);
        message.setContent(content);
        message.setTimestamp(LocalDateTime.now());

        return messageRepository.save(message);
    }

    @Override
    public Optional<Message> getMessage(UUID messageId) {
        return messageRepository.findById(messageId);
    }

    @Override
    public List<Message> getMessagesByChat(UUID chatId) {
        return messageRepository.findByChat_IdOrderByTimestampAsc(chatId);
    }

    @Override
    public void deleteMessage(UUID messageId, UUID authenticatedUserId) {
        Message message = messageRepository.findById(messageId)
                        .orElseThrow(()-> new IllegalArgumentException("Message not found"));

        //Only sender can delete their own message
        if(!message.getSender().getId().equals(authenticatedUserId)){
            throw new IllegalArgumentException("You can only delete your own messages");
        }

        messageRepository.delete(message);
    }

    @Override
    public Message editMessage(UUID messageId, UUID authenticatedUserId, String newContent) {
        Message message = messageRepository.findById(messageId)
                    .orElseThrow(()-> new IllegalArgumentException("Message not found"));

        //Only the sender can edit their own message
        if(!message.getSender().getId().equals(authenticatedUserId)){
            throw new IllegalArgumentException("You can only edit your own messages");
        }

        message.setContent(newContent);
        message.setTimestamp(LocalDateTime.now());
        //isEdited flag needs to be added

        return messageRepository.save(message);
    }
}
