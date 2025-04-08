package com.cofix.cofixBackend.Models;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class PostPk implements Serializable {
    private String email;
    private Long postId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PostPk that = (PostPk) o;
        return email.equals(that.email) && postId.equals(that.postId);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(email, postId);
    }
}
