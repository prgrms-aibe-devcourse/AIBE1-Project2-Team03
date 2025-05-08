package aibe.hosik.user;

import aibe.hosik.profile.Profile;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Table(name = "users")
public class User implements UserDetails {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @Column(nullable = false, unique = true)
  private String username;
  @Column(nullable = true)
  private String password;
  @Column(nullable = false)
  private String email;
  @Column(nullable = false)
  private String name;
  @Enumerated(EnumType.STRING)
  @Column(name = "roles", nullable = false)
  private Role roles;
  @Enumerated(EnumType.STRING)
  @Column(nullable = true)
  private SocialType socialType;
  @Column(nullable = true, unique = true)
  private String socialId;

  // User와 양방향
  @OneToOne(mappedBy = "user", fetch = FetchType.LAZY)
  @JsonIgnore
  private Profile profile;
  
  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return Collections.singleton(new SimpleGrantedAuthority(this.roles.name()));
  }
  @Override
  public boolean isAccountNonExpired() {
      return true;
  }
  @Override
  public boolean isAccountNonLocked() {
      return true;
  }
  @Override
  public boolean isCredentialsNonExpired() {
      return true;
  }
  @Override
  public boolean isEnabled() {
      return true;
  }
}