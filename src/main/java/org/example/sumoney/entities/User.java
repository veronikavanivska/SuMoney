package org.example.sumoney.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.Id;


import java.util.ArrayList;
import java.util.List;

@Data
@Table(name = "users")
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;
    private String password;


    @Column(name = "token_version", nullable = false)
    private Long tokenVersion = 0L;

    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Delegation> delegation = new ArrayList<>();

    public void incrementTokenVersion() {
        this.tokenVersion++;
    }
}