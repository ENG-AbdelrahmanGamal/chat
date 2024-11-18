package com.example.chat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {
    private EditText messageInput;
    private Button sendButton;
    private ListView messagesListView;

    private DatabaseReference chatDatabase;
    private String userId = "User1"; // Simulating two users: User1 and User2
    private String otherUserId = "User2"; // Change as needed

    private List<String> messagesList = new ArrayList<>();
    private MessagesAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);
        messagesListView = findViewById(R.id.messagesListView);

        chatDatabase = FirebaseDatabase.getInstance().getReference("chats");

        // Adapter for displaying messages
        adapter = new MessagesAdapter(this, messagesList);
        messagesListView.setAdapter(adapter);

        // Load messages
        loadMessages();

        // Send button listener
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = messageInput.getText().toString().trim();
                if (!TextUtils.isEmpty(message)) {
                    sendMessage(message);
                } else {
                    Toast.makeText(ChatActivity.this, "Enter a message", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loadMessages() {
        chatDatabase.child(userId + "_" + otherUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messagesList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    String message = data.child("message").getValue(String.class);
                    messagesList.add(message);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ChatActivity.this, "Failed to load messages", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendMessage(String message) {
        // Save message to database
        Map<String, Object> messageData = new HashMap<>();
        messageData.put("sender", userId);
        messageData.put("message", message);

        chatDatabase.child(userId + "_" + otherUserId).push().setValue(messageData).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                messageInput.setText(""); // Clear input field
            } else {
                Toast.makeText(ChatActivity.this, "Failed to send message", Toast.LENGTH_SHORT).show();
            }
        });
    }
}