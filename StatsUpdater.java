import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.PieChart;
import org.knowm.xchart.PieChartBuilder;
import org.knowm.xchart.internal.chartpart.Chart;
import org.knowm.xchart.style.PieStyler;
import org.knowm.xchart.style.Styler;

public class StatsUpdater implements Runnable {
  String lastLastUpdate = "";
  
  int iterations = -1;
  
  JDAevents main;
  
  int cachedmembers = 0;
  
  public StatsUpdater(JDAevents main) {
    this.main = main;
  }
  
  public void buildAndPutLeaderboardEmbed(TreeMap<Integer, List<String>> map, String sorted, String queryPut) {
    String embedmainmessage = "**Below is a list of countries sorted by " + sorted + ". You can see more detailed information on a country by doing** /covid countryname**.**\n";
    int leaderboardamount = 0;
    EmbedBuilder countriesEmbed = new EmbedBuilder();
    countriesEmbed.setTitle(" ");
    countriesEmbed.setColor(new Color(15610628));
    countriesEmbed.setFooter(this.lastLastUpdate);
    countriesEmbed.setAuthor("Descending list of country statistics");
    File f = new File(queryPut + ".png");
    try {
      f.createNewFile();
    } catch (IOException e) {
      e.printStackTrace();
    } 
    PieChart pchart = ((PieChartBuilder)((PieChartBuilder)((PieChartBuilder)((PieChartBuilder)(new PieChartBuilder()).width(950)).height(950)).title("Distribution of countries by " + sorted + ".")).theme(Styler.ChartTheme.GGPlot2)).build();
    ((PieStyler)pchart.getStyler()).setLegendVisible(false);
    ((PieStyler)pchart.getStyler()).setAnnotationType(PieStyler.AnnotationType.LabelAndPercentage);
    ((PieStyler)pchart.getStyler()).setAnnotationDistance(1.25D);
    ((PieStyler)pchart.getStyler()).setPlotContentSize(0.6D);
    ((PieStyler)pchart.getStyler()).setStartAngleInDegrees(90.0D);
    int world = 0;
    int notOther = 0;
    label32: for (Integer i : map.descendingKeySet()) {
      for (String i2 : map.get(i)) {
        String now = embedmainmessage + "\n " + leaderboardamount + ". **" + i2 + "**: " + i;
        if (now.length() < 1500) {
          if (leaderboardamount == 0)
            world = i.intValue(); 
          if (leaderboardamount >= 1 && leaderboardamount <= 7) {
            pchart.addSeries(i2, i);
            notOther += i.intValue();
          } 
          embedmainmessage = now;
          leaderboardamount++;
          continue;
        } 
        break label32;
      } 
    } 
    pchart.addSeries("Other", Integer.valueOf(world - notOther));
    try {
      BitmapEncoder.saveBitmap((Chart)pchart, f.getName(), BitmapEncoder.BitmapFormat.PNG);
    } catch (IOException e) {
      e.printStackTrace();
    } 
    countriesEmbed.setImage("attachment://" + queryPut + ".png");
    countriesEmbed.setDescription(embedmainmessage);
    this.main.getClass();
    countriesEmbed.addField(" ", "[Support Server](https://discord.gg/v8qDQDc) | [Add this bot to a server](https://discordapp.com/api/oauth2/authorize?client_id=675390513020403731&permissions=8&scope=bot) | [Vote for the bot](https://top.gg/bot/675390513020403731)", false);
    if (this.iterations == 0)
      System.out.println(countriesEmbed.length()); 
    this.main.top.put(queryPut, countriesEmbed.build());
  }
  
  public void run() {
    (new Timer()).schedule(new TimerTask() {
          public void run() {
            Element table, contentInner;
            long days = 0L;
            try {
              SimpleDateFormat myFormat = new SimpleDateFormat("dd MM yyyy");
              Date date1 = myFormat.parse("07 02 2020");
              Date date2 = new Date();
              long diff = date2.getTime() - date1.getTime();
              days = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
            } catch (ParseException e) {
              e.printStackTrace();
            } 
            EmbedBuilder stats = new EmbedBuilder();
            stats.setTitle(" ");
            stats.setColor(new Color(15610628));
            stats.addField(" ", "The bot is in **" + StatsUpdater.this.main.jda.getGuilds().size() + "** servers, with a total of **" + StatsUpdater.this.main.jda.getUsers().size() + "** members, running across **" + StatsUpdater.this.main.jda.getShardsTotal() + "** shards.", false);
            stats.addField(" ", "The bot has been called for stats **" + StatsUpdater.this.main.uses + "** times. (Since update v2.1)", false);
            stats.addField(" ", "The bot is also auto-updating in **" + StatsUpdater.this.main.channelsToAuto.size() + "** channels.", false);
            stats.addField(" ", "The bot was created **" + String.valueOf(days) + "** days ago.", false);
            StatsUpdater.this.cachedmembers = (StatsUpdater.this.main.jda.getGuildById(675390855716274216L) == null) ? StatsUpdater.this.cachedmembers : StatsUpdater.this.main.jda.getGuildById(675390855716274216L).getMemberCount();
            stats.addField(" ", "The bot's support server has **" + StatsUpdater.this.cachedmembers + "** members.", false);
            StatsUpdater.this.main.getClass();
            stats.addField(" ", "[Support Server](https://discord.gg/v8qDQDc) | [Add this bot to a server](https://discordapp.com/api/oauth2/authorize?client_id=675390513020403731&permissions=8&scope=bot) | [Vote for the bot](https://top.gg/bot/675390513020403731)", false);
            stats.setAuthor("Bot Statistics", null, null);
            StatsUpdater.this.main.stats = stats.build();
            Document doc = null;
            try {
              System.out.println("Fetching worldometers.");
              doc = Jsoup.connect("https://www.worldometers.info/coronavirus/").timeout(25000).get();
              System.out.println("Done fetching worldometers.");
              if (doc == null)
                throw new Exception("cant fockin get the doc mate"); 
              contentInner = (Element)doc.getElementsByAttributeValue("class", "content-inner").get(0);
              table = (Element)doc.getElementsByAttributeValue("id", "main_table_countries_today").get(0);
            } catch (Exception e) {
              System.out.println("Could not fetch stats!" + e.getMessage());
              return;
            } 
            StatsUpdater.this.iterations++;
            String lastUpdate = ((Element)contentInner.getElementsByAttributeValue("style", "font-size:13px; color:#999; margin-top:5px; text-align:center").get(0)).text();
            if (!StatsUpdater.this.lastLastUpdate.equals(lastUpdate) || StatsUpdater.this.iterations == 0) {
              StatsUpdater.this.lastLastUpdate = lastUpdate;
              TreeMap<Integer, List<String>> totalCountries = new TreeMap<>();
              TreeMap<Integer, List<String>> activeCountries = new TreeMap<>();
              TreeMap<Integer, List<String>> deathsCountries = new TreeMap<>();
              TreeMap<Integer, List<String>> recoveredCountries = new TreeMap<>();
              for (Element e : table.child(1).children()) {
                String countryUsing;
                if (e.child(1).text().equals("Diamond Princess"))
                  continue; 
                if (e.className().contains("row_continent"))
                  continue; 
                Element x = e;
                String countryFromWorldometers = x.child(1).text();
                String total = x.child(2).text();
                total = total.equals("") ? "0" : total;
                String newTotal = x.child(3).text();
                String deaths = x.child(4).text();
                deaths = deaths.equals("") ? "0" : deaths;
                String newDeaths = x.child(5).text();
                String recovered = x.child(6).text();
                recovered = recovered.equals("") ? "0" : recovered;
                String active = x.child(7).text();
                active = active.equals("") ? "0" : active;
                EmbedBuilder embed = new EmbedBuilder();
                embed.setTitle(" ");
                embed.setColor(new Color(15610628));
                embed.addField("**Current Cases**:", active, true);
                embed.addField("**Total Deaths**:", deaths + " (" + (newDeaths.equals("") ? "+0" : newDeaths) + " today)", true);
                embed.addField("**Total Recoveries**:", recovered, true);
                embed.addField("**Total Cases**:", total + " (" + (newTotal.equals("") ? "+0" : newTotal) + " today)", false);
                StatsUpdater.this.main.getClass();
                embed.addField(" ", "[Support Server](https://discord.gg/v8qDQDc) | [Add this bot to a server](https://discordapp.com/api/oauth2/authorize?client_id=675390513020403731&permissions=8&scope=bot) | [Vote for the bot](https://top.gg/bot/675390513020403731)", false);
                String iso2code = StatsUpdater.this.main.findCountry(countryFromWorldometers);
                if (iso2code != null) {
                  countryUsing = StatsUpdater.this.main.CodesToNames.get(iso2code);
                  embed.setAuthor("Current COVID-19 Statistics for " + countryUsing, null, "https://www.countryflags.io/" + iso2code + "/shiny/64.png");
                } else {
                  countryUsing = countryFromWorldometers;
                  embed.setAuthor("Current COVID-19 Statistics for " + countryUsing, null, "https://www.pngarts.com/files/5/World-Transparent-Background-PNG.png");
                } 
                Integer tI = Integer.valueOf(Integer.parseInt(total.replace(",", "").replace("N/A", "0")));
                Integer aI = Integer.valueOf(Integer.parseInt(active.replace(",", "").replace("N/A", "0")));
                Integer dI = Integer.valueOf(Integer.parseInt(deaths.replace(",", "").replace("N/A", "0")));
                Integer rI = Integer.valueOf(Integer.parseInt(recovered.replace(",", "").replace("N/A", "0")));
                List<String> tC = totalCountries.get(tI);
                List<String> aC = activeCountries.get(aI);
                List<String> dC = deathsCountries.get(dI);
                List<String> rC = recoveredCountries.get(rI);
                if (tC == null)
                  tC = new ArrayList<>(); 
                if (aC == null)
                  aC = new ArrayList<>(); 
                if (dC == null)
                  dC = new ArrayList<>(); 
                if (rC == null)
                  rC = new ArrayList<>(); 
                tC.add(countryUsing);
                aC.add(countryUsing);
                dC.add(countryUsing);
                rC.add(countryUsing);
                totalCountries.put(tI, tC);
                activeCountries.put(aI, aC);
                deathsCountries.put(dI, dC);
                recoveredCountries.put(rI, rC);
                embed.setFooter(lastUpdate);
                StatsUpdater.this.main.currentEmbeds.put(countryUsing, embed.build());
              } 
              StatsUpdater.this.buildAndPutLeaderboardEmbed(totalCountries, "total cases", "total");
              StatsUpdater.this.buildAndPutLeaderboardEmbed(activeCountries, "active cases", "active");
              StatsUpdater.this.buildAndPutLeaderboardEmbed(deathsCountries, "total deaths", "deaths");
              StatsUpdater.this.buildAndPutLeaderboardEmbed(recoveredCountries, "total recoveries", "recovered");
            } 
            if (StatsUpdater.this.iterations != 0) {
              ArrayList<Long> markedForRemoval = new ArrayList<>();
              for (Long l : StatsUpdater.this.main.channelsToAuto.keySet()) {
                if (StatsUpdater.this.main.jda.getTextChannelById(l.longValue()) != null) {
                  AutoUpdateData data = StatsUpdater.this.main.channelsToAuto.get(l);
                  if (StatsUpdater.this.iterations % data.interval == 0)
                    try {
                      StatsUpdater.this.main.jda.getTextChannelById(l.longValue()).sendMessage(StatsUpdater.this.main.currentEmbeds.get(data.country)).queue();
                    } catch (InsufficientPermissionException e) {
                      System.out.println("removing (no perms) " + StatsUpdater.this.main.jda.getTextChannelById(l.longValue()).getName());
                      markedForRemoval.add(l);
                    } catch (IllegalArgumentException e) {
                      System.out.println("removing (bad autoupdatedata) " + StatsUpdater.this.main.jda.getTextChannelById(l.longValue()).getName());
                      markedForRemoval.add(l);
                    }  
                } 
              } 
              for (Long l : markedForRemoval)
                StatsUpdater.this.main.channelsToAuto.remove(l); 
              StatsUpdater.this.main.pool.execute(new Runnable() {
                    public void run() {
                      StatsUpdater.this.main.saveHashMap("guildAutoUpdateData.dat", StatsUpdater.this.main.channelsToAuto);
                    }
                  });
            } 
            EmbedBuilder mapEmbed = new EmbedBuilder();
            mapEmbed.setTitle(" ");
            mapEmbed.setColor(new Color(15610628));
            mapEmbed.setAuthor("Map of currently infected regions", null, null);
            mapEmbed.setImage("attachment://map.png");
            StatsUpdater.this.main.getClass();
            mapEmbed.addField(" ", "[Support Server](https://discord.gg/v8qDQDc) | [Add this bot to a server](https://discordapp.com/api/oauth2/authorize?client_id=675390513020403731&permissions=8&scope=bot) | [Vote for the bot](https://top.gg/bot/675390513020403731)", false);
            mapEmbed.setFooter(lastUpdate);
            StatsUpdater.this.main.map = mapEmbed.build();
            StatsUpdater.this.main.jda.setPresence(OnlineStatus.ONLINE, Activity.watching(StatsUpdater.this.main.jda.getGuilds().size() + " servers for /help"));
          }
        }0L, 120000L);
  }
}

