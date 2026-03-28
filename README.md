# Buy Price Tracker (Fabric)

A client-side Fabric mod that stores an auction purchase price + purchase time on items and appends those values to each item tooltip.

## How it works

1. When connected to Hypixel, the mod periodically polls:
   - `https://api.hypixel.net/v2/skyblock/auctions_ended`
2. It keeps recently ended auctions bought by your own player UUID.
3. When you click an item in a GUI with `auction` in the title:
   - It first tries to match against the recent Hypixel ended-auction records by item name.
   - If no Hypixel match exists, it falls back to parsing lore price text from the clicked item.
4. On first successful detection it writes NBT under:
   - `buypricetracker.bought_price`
   - `buypricetracker.bought_at` (epoch milliseconds)
5. Tooltips always append:
   - `Bought for: X coins`
   - `Bought at: yyyy-MM-dd HH:mm:ss z`

The values are written once and remain on that item forever as long as this mod is installed.

## Notes

- This is client-side.
- Hypixel matching uses a short recency window and normalized item name matching, then falls back to lore parsing for other auction implementations.
