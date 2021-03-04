package ru.invitro.automation.notification.telegram;

import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.ApiContext;
import ru.invitro.automation.notification.config.ConfigReader;
import ru.invitro.automation.notification.telegram.logger.Logger;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

public class BotFactory {

    private static Bot bot = null;

    /**
     * If config has non empty ProxyServer and non zero ProxyPort bot will be use a proxy for connection
     * If config has non empty ProxyUser and ProxyPassword bot will be use this credential for connection to proxy server
     * ProxyType param in config file must be NO_PROXY, HTTP, SOCKS4 or SOCKS5. If ProxyType has other value bot will be use NO_PROXY
     * @return configured bot
     */
    public static Bot getBot() {
        if (bot == null) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Logger.writeLog(e.getMessage(), "main");
                e.printStackTrace();
            }
            DefaultBotOptions botOptions = ApiContext.getInstance(DefaultBotOptions.class);
            if (!ConfigReader.config.getProxyServer().equals("") && ConfigReader.config.getProxyPort() > 0) {
                if (!ConfigReader.config.getProxyUser().equals("") && !ConfigReader.config.getProxyPassword().equals("")) {
                    Authenticator.setDefault(new Authenticator() {
                        @Override
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(ConfigReader.config.getProxyUser(), ConfigReader.config.getProxyPassword().toCharArray());
                        }
                    });
                }
                botOptions.setProxyHost(ConfigReader.config.getProxyServer());
                botOptions.setProxyPort(ConfigReader.config.getProxyPort());
                try {
                    botOptions.setProxyType(DefaultBotOptions.ProxyType.valueOf(ConfigReader.config.getProxyType()));
                } catch (IllegalArgumentException e) {
                    botOptions.setProxyType(DefaultBotOptions.ProxyType.NO_PROXY);
                }
            }
            bot = new Bot(botOptions);
        }
        return bot;
    }
}
