package xyz.xisyz.application.dto.request.token;

public record OAuth2GitHubREQ(
        Integer id,
        String login,
        String avatar_url
) {
}