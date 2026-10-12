# Bölüm 1: Spec-Driven Development (SDD) & Google Mülakat Metodolojisi

## 1. Kıdemli Mühendislikte Zihniyet Değişimi

Google'da **Senior Software Engineer (L5)** veya **Staff Software Engineer (L6)** pozisyonları için mülakata girdiğinizde başarı kriteri kökten değişir:
* **L3/L4 (Junior/Mid Seviye)**: Verilen net bir problemi LeetCode tarzı test senaryolarından geçirmek ve temel karmaşıklık analizi bilmek yeterlidir.
* **L5/L6 (Senior/Staff Seviye)**: Mülakatçı size **bilerek eksik tanımlanmış, belirsiz (underspecified) bir problem** sunar. Kapsamı ve kısıtları netleştirmeden doğrudan kod yazmaya başlamak doğrudan elenme (Red Flag) sebebidir.

Kıdemli bir mühendisin mülakatı, bir mimari tasarım incelemesini (Design Review / RFC) yöneten Teknik Lider gibi yönlendirmesi beklenir:
1. **Sınırları ve Kapsamı Netleştirme**: Kod yazmadan önce girdi/çıktı boyutlarını ve kısıtları belirlemek.
2. **Resmi Matematiksel Spesifikasyon**: Ön koşullar, son koşullar ve durum değişmezleri (state invariants) oluşturmak.
3. **Katı Kaynak ve Karmaşıklık Bütçesi**: Asimptotik zaman ve bellek sınırlarını baştan ilan etmek.
4. **Test Kontratı Tasarımı**: Uç durumlar (Edge Cases), dejenere ağaçlar ve çakışma senaryolarını listelemek.
5. **Modern Java 25 ile Modüler Kodlama**: Record tipleri, pattern matching ve bellek dostu ilkel diziler kullanmak.
6. **Sistematik Doğrulama & Çıkarımlar**: Değişken izleme tablosu (trace table) ve proaktif mimari takas (trade-off) analizi yapmak.

---

## 2. 6 Aşamalı SDD Protokolü (45 Dakikalık Plan)

```
00:00 ---------------------- 05:00 ---------------------- 12:00 ---------------------- 32:00 ---------------------- 40:00 --------- 45:00
  |                            |                            |                            |                            |                 |
  Aşama 1: Kapsam & Belirsizlik Aşama 2: Spesifikasyon     Aşama 3: Bütçe & Testler     Aşama 4: Java 25 Kodlama     Aşama 5: İzleme   Aşama 6: Kapanış
```

### Aşama 1: Belirsizliği Giderme & Sınır Analizi (0 - 5. Dakika)
Mülakatçılar sizin **Genel Bilişsel Yeteneğinizi (GCA)** ve **Rol Bilginizi (RRK)** belirsizliği nasıl parçaladığınıza bakarak puanlar:
* **Veri Hacmi ve Ölçeği ($N$)**:
  * $N \le 20 \implies O(2^N)$ Bitmask DP / Backtracking kabul edilebilir.
  * $N \le 10^3 \implies O(N^2)$ algoritmalar (Floyd-Warshall, 2D DP) çalışır.
  * $N \le 10^5 \implies O(N \log N)$ veya $O(N \sqrt{N})$ zorunludur (Segment Tree, Binary Lifting, Sıralama).
  * $N \le 10^7 \implies O(N)$ doğrusal veya $O(N)$ amortize süre gereklidir (Fenwick, Monotonic Deque, Dinic).
  * $N \ge 10^9 \implies$ Streaming algoritmaları, harici bellek veya olasılıksal veri yapıları (Bloom Filter, HyperLogLog).
* **Veri Tipleri**: Negatif sayılar olabilir mi? Değerler 32-bit `int` sınırında mı kalacak yoksa toplamlar 64-bit `long` mu gerektirecek?
* **Erişim Deseni & Eşzamanlılık**: Bu yapı çevrimdışı tek bir thread tarafından mı işlenecek, yoksa binlerce sanal thread (virtual thread) tarafından eşzamanlı mı okunup yazılacak?

### Aşama 2: Resmi Teknik Spesifikasyon & Değişmezler (5 - 10. Dakika)
Kod yazmadan önce arayüzü ve matematiksel durum değişmezini beyaz tahtaya yazın:

```java
// Resmi sözleşme
public sealed interface RangeAggregator permits SegmentTreeWithLazy {
    void updateRange(int left, int right, long delta);
    long querySum(int left, int right);
    long queryMin(int left, int right);
}
```

Matematiksel Değişmezi (State Invariant) yüksek sesle ifade edin:
> *"Her $i$ düğümü $[L, R]$ aralığını temsil eder ve `treeSum[i]` değeri strictly sol ve sağ çocuklarının toplamına eşittir. Ertelenen güncellemeler `lazy[i]` dizisinde tutulur ve çocuklara inilmeden önce mutlaka aşağı itilmelidir (`pushDown`)."*

### Aşama 3: Karmaşıklık Bütçesi & Test Kontratı (10 - 15. Dakika)
Hedef bütçenizi önceden ilan edin:
* Zaman: Sorgu ve güncelleme başına $O(\log N)$.
* Alan: $4N$ ilkel dizi, işaretçi (pointer) maliyeti ve GC tahsisi sıfır.

Test Kontratı Matrisi:
1. **Minimal Girdiler**: $N = 0$, $N = 1$.
2. **Sınır Aralıkları**: Tek elemanlık aralık $[k, k]$, tüm dizi $[0, N-1]$.
3. **Ekstrem Değerler**: `Long.MAX_VALUE` değerine yakın sayılar, negatif farklar.
4. **Çakışmalı / Stres Girdiler**: Lazy mekanizmasını zorlayacak ardışık güncelleme ve sorgular.

### Aşama 4: Java 25 ile Üretim Kalitesinde Kodlama (15 - 35. Dakika)
Temiz ve modüler kod yazın:
* Değişmez durumlar için Java 25 Record yapıları kullanın.
* Sealed interfaceler ve switch pattern matching uygulayın.
* Wrapper sınıflar (`Long`, `Integer`) yerine ilkel diziler (`long[]`, `int[]`) tercih ederek CPU L1/L2 önbellek satırını (Cache Line Locality) maksimize edin.

### Aşama 5: İzleme Tablosu ile Doğrulama (35 - 40. Dakika)
Asla kodu bitirir bitirmez "bitti" demeyin. Bir test senaryosu seçin ve bir değişken izleme tablosu ile adım adım kod üzerinden geçin:
* `mid = (l + r) >>> 1` işlemlerinde taşma ve off-by-one hatalarını kontrol edin.
* Dizi sınır kontrollerini doğrulayın.

### Aşama 6: Kıdemli Değerlendirmesi & Mimari Takaslar (40 - 45. Dakika)
Mülakatçının sormasını beklemeden katma değerli teknik yorumlar ekleyin:
* *"Eğer üretim ortamında aralık güncellemeleri yerine sadece tekil nokta güncellemeleri olsaydı, Fenwick Ağacı bellek kullanımını %75 azaltır ve önbellek yerelliğini artırırdı."*
* *"Eğer bu veri yapısına binlerce sanal thread eşzamanlı yazacaksa, kilit çekişmesini (lock contention) önlemek için kilit dilimleme (lock striping) uygulayabiliriz."*

---

## 3. Google L5/L6 Mülakat Değerlendirme Rubriği

| Kriter | Puan 1 (No Hire) | Puan 2 (Leaning No Hire) | Puan 3 (Leaning Hire) | Puan 4 (Strong Hire - L5/L6) |
| :--- | :--- | :--- | :--- | :--- |
| **Problem Çözme (GCA)** | Belirsizlikte tıkanır; sürekli ipucu bekler. | Kaba kuvvet çözer; optimizasyonda zorlanır. | Küçük bir ipucuyla optimum çözüme ulaşır. | Belirsizliği anında parçalar, 2-3 yaklaşımı takaslarıyla karşılaştırıp otonom olarak en iyiye ulaşır. |
| **Kodlama & RRK** | Hatalı kod; Java koleksiyonlarını bilmez. | Çalışan ama dağınık kod; ham tipler kullanır. | Temiz, çalışan Java kodu; testleri geçer. | Üretim seviyesi Java 25; değişmezleri korur, defensive copy yapar, CPU önbellek yerelliğini gözetir. |
| **Sistem/Ölçek Sezgisi** | Ölçek ve donanım etkilerini yok sayar. | Big-O bilir ama donanım gerçeklerini unutur. | Zaman/alan karmaşıklığını doğru hesaplar. | CPU cache-line, GC baskısı, false sharing, eşzamanlılık ve dağıtık ölçeklemeyi proaktif tartışır. |
| **Liderlik & İletişim** | Düşünürken sessiz kalır; eleştirilince savunmaya geçer. | Sadece soru sorulunca konuşur; geç adapte olur. | Yaklaşımını net açıklar; geri bildirimi dinler. | Oturumu bir teknik lider gibi yönetir; nedenlerini önceden sesli düşünerek net açıklar. |
