package me.daoge.aconomy.command;

import me.daoge.aconomy.api.AcoEconomyAPI;
import org.allaymc.api.command.Command;
import org.allaymc.api.command.SenderType;
import org.allaymc.api.command.tree.CommandNode;
import org.allaymc.api.command.tree.CommandTree;
import org.allaymc.api.entity.interfaces.EntityPlayer;
import org.allaymc.api.permission.OpPermissionCalculator;
import org.allaymc.api.utils.TextFormat;
import org.allaymc.economyapi.Account;
import org.allaymc.economyapi.Currency;
import org.allaymc.economyapi.EconomyAPI;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Main command for Aconomy plugin.
 * Provides all economy-related subcommands.
 *
 * @author daoge_cmd
 */
public class AcoCommand extends Command {

    public AcoCommand() {
        super("aconomy", "Aconomy economy command", "aconomy.command");
        this.aliases.add("aco");
        OpPermissionCalculator.NON_OP_PERMISSIONS.addAll(Set.of(
                "aconomy.command",
                "aconomy.command.balance",
                "aconomy.command.transfer",
                "aconomy.command.top"
        ));
    }

    /**
     * Helper method to get player name from EntityPlayer.
     */
    private static String getPlayerName(EntityPlayer entityPlayer) {
        var controller = entityPlayer.getController();
        if (controller != null) {
            return controller.getOriginName();
        }
        return entityPlayer.getUniqueId().toString();
    }

    @Override
    public void prepareCommandTree(CommandTree tree) {
        CommandNode root = tree.getRoot();

        // /aconomy balance [player]
        root.key("balance")
                .permission("aconomy.command.balance")
                .playerTarget("player")
                .optional()
                .exec((context, player) -> {
                    List<EntityPlayer> targets = context.getResult(1);
                    if (targets == null || targets.isEmpty()) {
                        // Query own balance
                        if (!player.isPlayer()) {
                            context.addInvalidExecutorError(SenderType.PLAYER);
                            return context.fail();
                        }

                        EntityPlayer entityPlayer = player.asPlayer();
                        UUID uuid = entityPlayer.getUniqueId();
                        Account account = EconomyAPI.getAPI().getOrCreateAccount(uuid);
                        Currency currency = EconomyAPI.getAPI().getDefaultCurrency();
                        BigDecimal balance = account.getBalance(currency);
                        context.addOutput(TextFormat.GREEN + "Your balance: " + TextFormat.YELLOW + currency.format(balance));
                        return context.success();
                    }

                    if (targets.size() > 1) {
                        context.addTooManyTargetsError();
                        return context.fail();
                    }

                    EntityPlayer target = targets.getFirst();
                    UUID targetUuid = target.getUniqueId();
                    Account account = EconomyAPI.getAPI().getOrCreateAccount(targetUuid);
                    Currency currency = EconomyAPI.getAPI().getDefaultCurrency();
                    BigDecimal balance = account.getBalance(currency);
                    context.addOutput(TextFormat.GREEN + getPlayerName(target) + "'s balance: " + TextFormat.YELLOW + currency.format(balance));
                    return context.success();
                }, SenderType.ANY);

        // /aconomy transfer <amount> <player>
        root.key("transfer")
                .permission("aconomy.command.transfer")
                .doubleNum("amount")
                .playerTarget("player")
                .exec((context, player) -> {
                    EntityPlayer entityPlayer = player.asPlayer();

                    double amountDouble = context.getResult(1);
                    if (amountDouble <= 0) {
                        context.addError("Amount must be positive!");
                        return context.fail();
                    }

                    BigDecimal amount = BigDecimal.valueOf(amountDouble);

                    List<EntityPlayer> targets = context.getResult(2);
                    if (targets == null || targets.isEmpty()) {
                        context.addPlayerNotFoundError();
                        return context.fail();
                    }

                    if (targets.size() > 1) {
                        context.addTooManyTargetsError();
                        return context.fail();
                    }

                    EntityPlayer target = targets.getFirst();
                    if (target.getUniqueId().equals(entityPlayer.getUniqueId())) {
                        context.addError("You cannot transfer money to yourself!");
                        return context.fail();
                    }

                    Currency currency = EconomyAPI.getAPI().getDefaultCurrency();
                    Account fromAccount = EconomyAPI.getAPI().getOrCreateAccount(entityPlayer.getUniqueId());
                    Account toAccount = EconomyAPI.getAPI().getOrCreateAccount(target.getUniqueId());

                    BigDecimal currentBalance = fromAccount.getBalance(currency);
                    if (currentBalance.compareTo(amount) < 0) {
                        context.addError("Insufficient balance! You have " + currency.format(currentBalance));
                        return context.fail();
                    }

                    boolean success = fromAccount.transfer(toAccount, currency, amount);
                    if (success) {
                        String targetName = getPlayerName(target);
                        context.addOutput(TextFormat.GREEN + "Successfully transferred " + TextFormat.YELLOW + currency.format(amount) + TextFormat.GREEN + " to " + TextFormat.YELLOW + targetName);

                        // Notify the recipient
                        var controller = target.getController();
                        if (controller != null) {
                            String senderName = getPlayerName(entityPlayer);
                            controller.sendMessage(TextFormat.GREEN + senderName + " transferred " + TextFormat.YELLOW + currency.format(amount) + TextFormat.GREEN + " to you!");
                        }
                    } else {
                        context.addError("Transfer failed!");
                        return context.fail();
                    }

                    return context.success();
                }, SenderType.ACTUAL_PLAYER);

        // /aconomy top [size]
        root.key("top")
                .permission("aconomy.command.top")
                .intNum("size", 10)
                .optional()
                .exec((context, sender) -> {
                    int size = context.getResult(1);
                    if (size <= 0) {
                        size = 10;
                    } else if (size > 100) {
                        size = 100;
                    }

                    AcoEconomyAPI api = (AcoEconomyAPI) EconomyAPI.getAPI();
                    Currency currency = api.getDefaultCurrency();
                    List<Account> topAccounts = api.getTopAccounts(size);

                    if (topAccounts.isEmpty()) {
                        context.addOutput(TextFormat.YELLOW + "No accounts found.");
                        return context.success();
                    }

                    StringBuilder sb = new StringBuilder();
                    sb.append(TextFormat.GREEN).append("Top ").append(topAccounts.size()).append(" Richest Players\n");

                    int rank = 1;
                    for (Account account : topAccounts) {
                        BigDecimal balance = account.getBalance(currency);
                        sb.append(TextFormat.YELLOW)
                                .append(rank)
                                .append(". ")
                                .append(TextFormat.WHITE)
                                .append(account.getName())
                                .append(": ")
                                .append(TextFormat.GOLD)
                                .append(currency.format(balance));
                        if (rank < topAccounts.size()) {
                            sb.append("\n");
                        }
                        rank++;
                    }

                    context.addOutput(sb.toString());
                    return context.success();
                }, SenderType.ANY);

        // /aconomy set <amount> [player]
        root.key("set")
                .permission("aconomy.command.set")
                .doubleNum("amount")
                .playerTarget("player")
                .optional()
                .exec((context, sender) -> {
                    double amountDouble = context.getResult(1);
                    if (amountDouble < 0) {
                        context.addError("Amount cannot be negative!");
                        return context.fail();
                    }
                    BigDecimal amount = BigDecimal.valueOf(amountDouble);

                    List<EntityPlayer> targets = context.getResult(2);
                    if (targets == null || targets.isEmpty()) {
                        if (sender.isPlayer()) {
                            EntityPlayer entityPlayer = sender.asPlayer();
                            Currency currency = EconomyAPI.getAPI().getDefaultCurrency();
                            Account account = EconomyAPI.getAPI().getOrCreateAccount(entityPlayer.getUniqueId());
                            account.setBalance(currency, amount);
                            context.addOutput(TextFormat.GREEN + "Your balance has been set to " + TextFormat.YELLOW + currency.format(amount));
                            return context.success();
                        }

                        context.addPlayerNotFoundError();
                        return context.fail();
                    }

                    if (targets.size() > 1) {
                        context.addTooManyTargetsError();
                        return context.fail();
                    }

                    EntityPlayer target = targets.get(0);
                    Currency currency = EconomyAPI.getAPI().getDefaultCurrency();
                    Account account = EconomyAPI.getAPI().getOrCreateAccount(target.getUniqueId());
                    account.setBalance(currency, amount);

                    context.addOutput(TextFormat.GREEN + getPlayerName(target) + "'s balance has been set to " + TextFormat.YELLOW + currency.format(amount));
                    return context.success();
                }, SenderType.ANY);

        // /aconomy deposit <amount> [player]
        root.key("deposit")
                .permission("aconomy.command.deposit")
                .doubleNum("amount")
                .playerTarget("player")
                .optional()
                .exec((context, sender) -> {
                    double amountDouble = context.getResult(1);
                    if (amountDouble <= 0) {
                        context.addError("Amount must be positive!");
                        return context.fail();
                    }

                    BigDecimal amount = BigDecimal.valueOf(amountDouble);

                    List<EntityPlayer> targets = context.getResult(2);
                    if (targets == null || targets.isEmpty()) {
                        if (sender.isPlayer()) {
                            EntityPlayer entityPlayer = sender.asPlayer();
                            Currency currency = EconomyAPI.getAPI().getDefaultCurrency();
                            Account account = EconomyAPI.getAPI().getOrCreateAccount(entityPlayer.getUniqueId());
                            account.deposit(currency, amount);
                            context.addOutput(TextFormat.GREEN + "Deposited " + TextFormat.YELLOW + currency.format(amount) + TextFormat.GREEN + " to your account");
                            return context.success();
                        }
                        context.addPlayerNotFoundError();
                        return context.fail();
                    }

                    if (targets.size() > 1) {
                        context.addTooManyTargetsError();
                        return context.fail();
                    }

                    EntityPlayer target = targets.getFirst();
                    Currency currency = EconomyAPI.getAPI().getDefaultCurrency();
                    Account account = EconomyAPI.getAPI().getOrCreateAccount(target.getUniqueId());
                    account.deposit(currency, amount);

                    context.addOutput(TextFormat.GREEN + "Deposited " + TextFormat.YELLOW + currency.format(amount) + TextFormat.GREEN + " to " + getPlayerName(target) + "'s account");
                    return context.success();
                }, SenderType.ANY);

        // /aconomy withdraw <amount> [player]
        root.key("withdraw")
                .permission("aconomy.command.withdraw")
                .doubleNum("amount")
                .playerTarget("player")
                .optional()
                .exec((context, sender) -> {
                    double amountDouble = context.getResult(1);
                    if (amountDouble <= 0) {
                        context.addError("Amount must be positive!");
                        return context.fail();
                    }

                    BigDecimal amount = BigDecimal.valueOf(amountDouble);

                    List<EntityPlayer> targets = context.getResult(2);
                    if (targets == null || targets.isEmpty()) {
                        if (sender.isPlayer()) {
                            EntityPlayer entityPlayer = sender.asPlayer();
                            Currency currency = EconomyAPI.getAPI().getDefaultCurrency();
                            Account account = EconomyAPI.getAPI().getOrCreateAccount(entityPlayer.getUniqueId());

                            BigDecimal currentBalance = account.getBalance(currency);
                            if (currentBalance.compareTo(amount) < 0) {
                                context.addError("Insufficient balance! Current balance: " + currency.format(currentBalance));
                                return context.fail();
                            }

                            account.withdraw(currency, amount);
                            context.addOutput(TextFormat.GREEN + "Withdrew " + TextFormat.YELLOW + currency.format(amount) + TextFormat.GREEN + " from your account");
                            return context.success();
                        }

                        context.addPlayerNotFoundError();
                        return context.fail();
                    }

                    if (targets.size() > 1) {
                        context.addTooManyTargetsError();
                        return context.fail();
                    }

                    EntityPlayer target = targets.getFirst();
                    String targetName = getPlayerName(target);
                    Currency currency = EconomyAPI.getAPI().getDefaultCurrency();
                    Account account = EconomyAPI.getAPI().getOrCreateAccount(target.getUniqueId());

                    BigDecimal currentBalance = account.getBalance(currency);
                    if (currentBalance.compareTo(amount) < 0) {
                        context.addError("Insufficient balance! " + targetName + "'s balance: " + currency.format(currentBalance));
                        return context.fail();
                    }

                    account.withdraw(currency, amount);
                    context.addOutput(TextFormat.GREEN + "Withdrew " + TextFormat.YELLOW + currency.format(amount) + TextFormat.GREEN + " from " + targetName + "'s account");
                    return context.success();
                }, SenderType.ANY);
    }
}
