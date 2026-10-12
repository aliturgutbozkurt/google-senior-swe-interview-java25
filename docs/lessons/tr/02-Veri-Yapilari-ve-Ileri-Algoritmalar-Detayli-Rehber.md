# Bölüm 2: İleri Seviye Veri Yapıları & Algoritmalar (Google L5/L6)

## 1. Graf Teorisi ve Ağ Akışları

### 1.1 Tarjan Algoritması: Kuvvetli Bağlı Bileşenler (SCC) ve Köprüler
Google altyapısında (Borg iş bağımlılıkları, Spanner transaction çemberleri, Google Maps yol ağları) döngü ve bağlılık analizleri vazgeçilmezdir.

```
       0 ----> 1
       ^     / |
       |    /  |
       |   v   v
       2 <     3 <---> 4
   [SCC 1: {0,1,2}]   [SCC 2: {3,4}]
```

#### Matematiksel Değişmezler
1. **DFS Keşif Sırası**: Her $u$ düğümüne artan bir keşif indeksi `ids[u]` atanır.
2. **Düşük Bağlantı Değeri (Low-Link)**: `low[u]`, $u$ düğümünden DFS yığını üzerindeki düğümlere ulaşılabilecek en küçük `ids` değeridir.
   $$low[u] = \min(ids[u], \min_{(u, v) \in E} low[v] \text{ [ziyaret edilmemişse]}, \min_{(u, v) \in E} ids[v] \text{ [yığındaysa]})$$
3. **SCC Kök Koşulu**: DFS geri dönerken `ids[u] == low[u]` ise, $u$ düğümü bir bileşenin köküdür. Yığından $u$'ya kadar olan düğümler çekilerek bileşen elde edilir.
4. **Köprü (Bridge) Koşulu**: Yönsüz bir grafta bir $(u, v)$ kenarı ancak ve ancak $low[v] > ids[u]$ olduğunda kritik bir köprüdür.

---

### 1.2 Dinic Algoritması: Maksimum Ağ Akışı & İki Kümeli Eşleme
Google cluster kaynak tahsisi ve makine yerleşiminde maksimum akış kurguları yaygındır.
* **Edmonds-Karp**: $O(V \cdot E^2)$ zaman alırken,
* **Dinic Algoritması**: Genel ağlarda $O(V^2 \cdot E)$, birim kapasiteli ağlarda (bipartite matching) $O(E \sqrt{V})$ sürede tamamlanır.

#### İki Aşamalı Akış Döngüsü
1. **BFS Seviye Grafı (Level Graph)**: Kaynaktan hedefe en kısa yolları belirler. Hedefe ulaşılamazsa akış sonlandırılır.
2. **DFS Blok Akışı (Blocking Flow)**: `level[v] == level[u] + 1` koşulunu sağlayan kenarlardan akış gönderilir. `work[u]` işaretçisi doyurulan kenarların tekrar taranmasını engelleyerek evre başına $O(V \cdot E)$ sağlar.

---

### 1.3 İndeksli Öncelik Kuyruğu (IPQ) ile Dijkstra
Standart `java.util.PriorityQueue`, $O(\log V)$ decrease-key desteklemez. Yoğun bir grafta ($E \approx V^2$) kuyruğa çiftler eklemek boyutu $E$'ye çıkararak $O(E \log E)$ zaman ve gereksiz bellek harcar.

#### IPQ Mimarisi
Üç adet $V$ boyutlu ilkel dizi koordine edilir:
* `heap[p]`: İkili yığında $p$ pozisyonundaki düğüm indeksi.
* `pos[v]`: $v$ düğümünün ikili yığındaki konumunu veren ters harita.
* `keys[v]`: Düğümün mevcut en kısa mesafesi.

Kenar gevşetilirken `ipq.decreaseKey(v, newDist)` çağrıldığında doğrudan `pos[v]` pozisyonundan $O(\log V)$ sürede `swim` yapılır; nesne tahsisi sıfırdır.

---

### 1.4 Topolojik Sıralama ve Yönlü Döngü Rekonstrüksiyonu
* **Kahn Algoritması**: Giriş derecesi (in-degree) 0 olan düğümler kuyruğa alınır. İşlenen düğüm sayısı $< V$ ise döngü vardır.
* **DFS 3-Renk Algoritması**:
  * Beyaz (0): Ziyaret edilmemiş
  * Gri (1): Mevcut DFS çağrı yığınında
  * Siyah (2): Keşfi tamamlanmış
  * Gri renkli bir düğüme kenar bulunursa bir **Geri-Kenar (Back-Edge)** tespit edilmiştir. `parent` dizisi geriye doğru taranarak döngü silsilesi tam olarak çıkarılır (Örn: `[A -> B -> C -> A]`).

---

## 2. İleri Seviye Ağaçlar & Aralık Sorguları

### 2.1 Tembel Yayılımlı Segment Ağacı (Lazy Propagation)
$N$ boyutlu bir dizide $O(\log N)$ aralık güncellemesi ve sorgusu sağlar.

```
                    [0, 3] (Toplam: 16)
                   /      \
             [0, 1]        [2, 3]
            /     \        /     \
          [0]     [1]    [2]     [3]
```

#### Tembel Etiket Değişmezi (Lazy Invariant)
Bir aralığı $[ql, qr]$ tamamen kapsayan bir düğüme gelindiğinde:
1. Düğüm toplamı anında güncellenir: $\text{treeSum}[node] \mathrel{+}= \Delta \times (R - L + 1)$
2. Çocuklara aktarılacak fark kaydedilir: $\text{lazy}[node] \mathrel{+}= \Delta$
3. **Kritik Kural**: Çocuklara inilmeden önce mutlaka $\text{pushDown}(node)$ çağrılarak bekleyen değerler aktarılır ve `lazy` sıfırlanır.

---

### 2.2 Fenwick Ağacı (İkili İndeksli Ağaç - 1D & 2D)
* **Düşük Bit Kuralı**: `(i & -i)` ifadesi sayının en küçük 2'nin kuvveti olan bitini izole eder.
* **Önek Toplamı**: `tree[i]` toplanır ve `i -= (i & -i)` ile ilerlenir.
* **Nokta Ekleme**: `tree[i]` değerine ekleme yapılır ve `i += (i & -i)` ile güncellenir.
* Sıfır işaretçi (pointer) maliyetiyle L1 önbellek dostudur.

---

### 2.3 Binary Lifting ile En Yakın Ortak Ata (LCA)
* `up[u][k]`: $u$ düğümünün $2^k$ uzaklıktaki atasıdır.
* **DP Geçişi**: $\text{up}[u][k] = \text{up}[\text{up}[u][k-1]][k-1]$
* Derinlikler eşitlenip eşzamanlı olarak yukarı zıplanarak $O(\log N)$ sürede LCA ve ağaç içi mesafe hesaplanır.

---

## 3. Dinamik Programlama & Monoton Kuyruklar

### 3.1 Bitmask DP (Gezgin Satıcı Problemi)
$N \le 20$ olduğunda şehirlerin ziyaret durumu 32-bit tamsayının bitlerinde saklanır.
$dp(mask, u)$: Ziyaret edilmiş şehirler kümesi $mask$ iken $u$ şehrinden başlayarak turu tamamlama maliyeti.
Zaman Karmaşıklığı: $O(2^N \cdot N^2)$, Bellek: $O(2^N \cdot N)$.

### 3.2 Monoton Deque ile DP Optimizasyonu
Aşağıdaki formdaki kayan pencere geçişleri:
$$dp[i] = \text{cost}[i] + \max_{j \in [i-k, i-1]} dp[j]$$
Çift uçlu kuyruk (`Deque`) ile değerleri azalan sırada tutarak pencereden çıkanları baştan, değeri küçük kalanları sondan atar. Toplam süre $O(N \cdot k)$ yerine amortize $O(N)$ seviyesine düşer.

---

## 4. Karakter Katarları (Strings)

* **Knuth-Morris-Pratt (KMP)**: Prefix fonksiyonu $\pi$ ile eşleşmeyen karakter durumunda metin işaretçisini geri sarmadan $O(N)$ arama yapar.
* **Aho-Corasick**: Trie üzerine kurulu hata linkleri ile milyonlarca kelimelik sözlükleri streaming metin üzerinde $O(|Text| + \text{eşleşmeler})$ sürede tespit eder.
* **Rabin-Karp Çift Rolling Hash**: $10^9+7$ ve $10^9+9$ asal modüloları ve 257 tabanıyla çakışma ihtimalini $10^{-18}$ altına indirerek $O(N \log N)$ sürede en uzun tekrarlayan alt-katarları bulur.
