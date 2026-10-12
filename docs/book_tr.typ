#set document(
  title: "Google Kıdemli Yazılım Mühendisi (L5/L6) Mülakat Rehberi",
  author: "Ali Turgut Bozkurt",
  date: auto,
)

#set page(
  paper: "a4",
  margin: (top: 2.5cm, bottom: 2.5cm, left: 2.5cm, right: 2.5cm),
  header: context {
    if here().page() > 1 {
      smallcaps[Google Kıdemli SWE (L5/L6) Mülakat Rehberi]
      h(1fr)
      [Java 25 & SDD]
    }
  },
  footer: context {
    if here().page() > 1 {
      align(center)[#here().page()]
    }
  }
)

#set text(
  font: "Helvetica",
  size: 10pt,
  lang: "tr",
)

#set par(justify: true, leading: 0.65em)

// Kapak Sayfası
#align(center + horizon)[
  #block(
    fill: rgb("#1a73e8"),
    inset: 3em,
    radius: 0.8em,
    width: 100%,
    [
      #text(fill: white, size: 24pt, weight: "bold")[
        Google Kıdemli Yazılım Mühendisi (L5/L6)
      ]
      #v(0.5em)
      #text(fill: rgb("#e8f0fe"), size: 16pt, weight: "medium")[
        Teknik Mülakat Master Rehberi
      ]
      #v(1em)
      #text(fill: rgb("#f1f3f4"), size: 11pt)[
        Spec-Driven Development • Java 25 • İleri Seviye Algoritmalar • Google Ölçeğinde Sistem Tasarımı • Modern Eşzamanlılık • Googleyness & Liderlik
      ]
    ]
  )

  #v(3em)
  #text(size: 13pt, weight: "bold")[Yazar: Ali Turgut Bozkurt] \
  #text(size: 11pt, fill: rgb("#5f6368"))[Google Teknik Mülakat Adayları (L5 / L6) İçin Hazırlanmıştır]
  
  #v(4em)
  #text(size: 10pt, fill: rgb("#70757a"))[Üretim Kalitesinde Kod • 30/30 Test Onaylı • Java 25 LTS Preview]
]

#pagebreak()

// İçindekiler
#outline(
  title: [İçindekiler],
  indent: 1.5em,
  depth: 3
)

#pagebreak()

= Bölüm 1: Spec-Driven Development (SDD) & Google Metodolojisi

== 1.1 Kıdemli Mühendislikte Zihniyet Değişimi
Google'da *Senior Software Engineer (L5)* veya *Staff Software Engineer (L6)* seviyesinde başarı kriteri kökten değişir:
- *L3/L4 (Junior/Mid)*: Verilen net bir problemi testlerden geçirmek yeterlidir.
- *L5/L6 (Senior/Staff)*: Mülakatçı bilerek *eksik tanımlanmış, belirsiz problemler* sunar. Kapsamı ve kısıtları netleştirmeden kod yazmaya başlamak doğrudan elenme sebebidir.

Kıdemli bir mühendisin mülakatı mimari tasarım oturumunu (RFC) yöneten bir Teknik Lider gibi yönlendirmesi beklenir:
1. *Sınırları ve Kapsamı Netleştirme*: Kod yazmadan önce girdi boyutlarını ($N$) ve kısıtları belirlemek.
2. *Resmi Matematiksel Spesifikasyon*: Ön/son koşullar ve durum değişmezleri (state invariants) oluşturmak.
3. *Katı Karmaşıklık Bütçesi*: Asimptotik zaman ve bellek sınırlarını baştan ilan etmek.
4. *Test Kontratı Tasarımı*: Uç durumlar ve çakışma senaryolarını listelemek.
5. *Modern Java 25 ile Modüler Kodlama*: Record'lar, sealed tipler ve bellek dostu diziler kullanmak.
6. *Sistematik Doğrulama*: İzleme tablosu (trace table) ve proaktif mimari takas analizi yapmak.

== 1.2 6 Aşamalı SDD Protokolü (45 Dakikalık Dağılım)
- *Aşama 1: Belirsizlik & Kapsam (0 - 5. dk)*: Veri hacmi ($N$), tipler, eşzamanlılık ve hata durumlarını netleştirin.
- *Aşama 2: Resmi Spesifikasyon & Değişmezler (5 - 10. dk)*: Arayüz ve matematiksel durum değişmezlerini yazın.
- *Aşama 3: Karmaşıklık Bütçesi & Test Kontratı (10 - 15. dk)*: Hedef sınırları (_O(N log N)_, _O(N)_) ve test matrisini belirleyin.
- *Aşama 4: Java 25 ile Kodlama (15 - 35. dk)*: Defensive, temiz ve bellek yerelliği yüksek kod yazın.
- *Aşama 5: İzleme Tablosu ile Doğrulama (35 - 40. dk)*: Değişken tablosu ile kodu adım adım doğrulayın.
- *Aşama 6: Kıdemli Değerlendirmesi & Takaslar (40 - 45. dk)*: CPU önbellek satırları, false sharing ve dağıtık ölçeklemeyi tartışın.

== 1.3 Google L5/L6 Değerlendirme Sütunları
- *Genel Bilişsel Yetenek (GCA)*: Belirsizliği anında parçalama, optimum algoritmaları sentezleme.
- *Rol Bilgisi (RRK)*: Java bellek modeli, CPU önbellek yerelliği, kilit çekişmeleri ve asimptotik optimallik.
- *Liderlik*: Talimat beklemeden yön belirleme; gerekçeleri net sesli düşünerek aktarma.
- *Googleyness*: Entelektüel alçakgönüllülük, suçlamasız kültür ve uzun vadeli mühendislik mükemmeliyeti.

#pagebreak()

= Bölüm 2: İleri Seviye Veri Yapıları & Algoritmalar (Java 25)

== 2.1 Graf Paketi
=== Tarjan Algoritması: Kuvvetli Bağlı Bileşenler (SCC) & Köprüler
- Tek geçişte _O(V + E)_ sürede yönlü graflarda SCC'leri, yönsüz graflarda kritik köprüleri tespit eder.
- *Matematiksel Değişmezler*:
  - Keşif sırası `ids[u]` kesinlikle artandır.
  - `low[u] = min(ids[u], min_{(u,v) in E} low[v])`.
  - `ids[u] == low[u]` ise `u` bir bileşenin köküdür.
  - Yönsüz grafta `low[v] > ids[u]` ise `(u, v)` bir köprüdür.

=== Dinic Maksimum Ağ Akışı Algoritması
- Genel ağlarda _O(V² E)_, iki kümeli eşlemede (bipartite matching) _O(E √V)_ sürede çalışır.
- *İki Aşamalı Döngü*:
  1. *BFS Seviye Grafı*: En kısa genişletici yolları kurar.
  2. *DFS Blok Akışı*: `work[u]` işaretçisi ile doyurulan kenarları atlayarak akışı iletir.

=== İndeksli Öncelik Kuyruğu (IPQ) ile Dijkstra
- Standart `PriorityQueue`'nun $O(E log E)$ şişkinliğini önleyerek kesin _O((V + E) log V)_ zaman ve _O(V)_ bellek harcar; _O(log V)_ decrease-key sağlar.

=== Topolojik Sıralama ve Döngü Yolu Rekonstrüksiyonu
- Kahn algoritması ve DFS 3-renk yaklaşımı; döngü varsa döngüyü oluşturan düğüm silsilesini geriye dönük çıkarır.

== 2.2 İleri Seviye Ağaçlar & Aralık Sorguları
=== Tembel Yayılımlı Segment Ağacı (Lazy Propagation)
- $N$ boyutlu dizide _O(log N)_ sürede aralık toplama, aralık toplamı ve aralık minimumu gerçekleştirir.
- *Lazy Değişmezi*: Düğüm toplamı anında güncellenir; bekleyen farklar çocuklara inilmeden önce `pushDown` ile aktarılır.

=== Fenwick Ağacı (1D & 2D)
- `(x & -x)` bit manipülasyonu ile _O(log N)_ sürede önek toplamı ve nokta güncellemesi yapar; sıfır işaretçi maliyetlidir.

=== Binary Lifting ile En Yakın Ortak Ata (LCA)
- _O(N log N)_ ön işleme ile `up[u][k]` $2^k$-ıncı ata tablosunu kurar; _O(log N)_ sürede LCA ve ağaç mesafesi sorgular.

=== Bitwise Trie (Maksimum XOR)
- 31-bit ikili trie; sayı akışında maksimum XOR çiftini sorgu başına _O(31)_ sürede bulur.

== 2.3 Dinamik Programlama & Monoton Kuyruklar
=== Bitmask DP
- Gezgin Satıcı Problemini (TSP) _O(2ⁿ N²)_ sürede memoization ve optimum tur yolu rekonstrüksiyonu ile çözer.

=== Monoton Kuyruk DP Optimizasyonu
- Kayan pencere DP geçişlerini azalan monoton deque kullanarak _O(N · k)_ yerine amortize _O(N)_ seviyesine indirir.

== 2.4 Karakter Katarları (Strings)
- *Knuth-Morris-Pratt (KMP)*: _O(M)_ prefix fonksiyonu ile metin işaretçisini geri sarmadan _O(N)_ arama yapar.
- *Aho-Corasick*: Trie ve hata linkleri ile çoklu desenleri tek geçişte _O(|Text| + matches)_ sürede bulur.
- *Rabin-Karp Çift Rolling Hash*: 1.000.000.007 ve 1.000.000.009 modüloları ile çakışmasız _O(N log N)_ en uzun kopya alt-katarı bulur.

== 2.5 Üretim Kalitesinde Bellek İçi Veri Yapıları
- *O(1) LFU Cache*: Frekans listeleri ve çift yönlü bağlı listelerle _O(1)_ get/put ve LRU tie-breaking.
- *O(1) LRU Cache*: Standart kütüphane kestirmesi olmadan ham çift yönlü bağlı liste + HashMap.
- *Dual-Heap Median Finder*: Dengeli iki yığın ile _O(log N)_ ekleme ve _O(1)_ medyan sorgusu.

#pagebreak()

= Bölüm 3: Modern Java 25 Eşzamanlılık, Bellek Modeli & Internals

== 3.1 Sanal Thread Mimarisi (Project Loom)
- *Kullanıcı Alanında Zamanlama*: Milyonlarca sanal thread işletim sistemi çekirdek thread'lerinden bağımsız çalışır.
- *Continuation Yielding*: Bloklayıcı I/O sırasında sanal thread taşıyıcı thread'den (carrier) unmount olur; CPU boşta kalmaz.
- *Thread Pinning*: I/O sırasında `synchronized` bloklarından kaçının; `ReentrantLock` tercih edin.

== 3.2 Java 25 Yapısal Eşzamanlılık (`StructuredTaskScope`)
- Asenkron thread sızıntılarını sözlüksel kapsam garantisiyle önler:
  - `StructuredTaskScope.open(Joiner.awaitAllSuccessfulOrThrow())`: Paralel fan-out ve fail-fast iptali.
  - `StructuredTaskScope.open(Joiner.anySuccessfulResultOrThrow())`: Hızlı replikayı alan spekülatif yarış.

== 3.3 Scoped Values (`ScopedValue`)
- `ThreadLocal` yerine geçer; bellek sızıntısını sıfırlar, kapsam içi değişmezdir ve sanal thread'lere _O(1)_ maliyetle aktarılır.

== 3.4 Lock-Free Veri Yapıları & Java Bellek Modeli (JMM)
- *Treiber Stack*: `AtomicReference` CAS döngüsü ve `Thread.onSpinWait()` ipucuyla kilit gerektirmeyen LIFO yığını.
- *Michael-Scott Queue*: Sentinel düğüm korumalı, 2 aşamalı CAS ve işbirlikli kuyruk ilerletme.
- *Volatile & Happens-Before*: StoreLoad bariyeri ekleyerek CPU mağaza tamponlarını boşaltır ve yeniden sıralamayı önler.
- *False Sharing*: 64-byte önbellek satırını paylaşan değişkenler çekişmeyi %90+ artırır; dolgu veya `@Contended` gerektirir.

== 3.5 Üretim Seviyesi Rate Limiter Algoritmaları
- *Token Bucket*: Arka plan thread'i olmadan tembel CAS yenilemeli kilitlenmesiz hız sınırlayıcı.
- *Leaky Bucket*: Sabit çıkış debili, sıçramaları engelleyen kova.
- *Sliding Window Counter*: Alt pencereli zaman dilimli sayaç halkası.

#pagebreak()

= Bölüm 4: Google Ölçeğinde Dağıtık Sistem Tasarımı Mimarileri

== 4.1 Donanım Gecikmeleri & Kapasite Kuralları
- RAM erişimi: 100 ns | NVMe SSD okuma: 250 µs | Veri merkezi içi RTT: 500 µs | Kıtalararası RTT: 40 - 100 ms.
- Günde 100 Milyon İstek ≈ 1.200 QPS | Günde 1 Milyar İstek ≈ 12.000 QPS (Zirve 30k QPS).

== 4.2 7 Google Ölçeğinde Mimari Tasarımı
=== 1. Global Multi-Region Dağıtık Hız Sınırlayıcı
- GFE/Envoy uçlarında yerel token kovalarıyla mikrosaniye seviyesinde kontrol.
- Redis kümesinden toplu token kiralama (batch leasing).
- Ağ bölünmesinde fail-open yerel koruma.

=== 2. Google Ölçeğinde Web Tarayıcısı (Web Crawler)
- URL Frontier (PageRank öncelik kuyrukları ve host politeness kuyrukları).
- Bellek içi DNS prefetching.
- 64-bit SimHash ve Scalable Bloom Filter ile içerik tekilleştirme.
- Colossus ve Bigtable depolama katmanı.

=== 3. Google Spanner Dağıtık Veritabanı
- TrueTime API: GPS ve atomik saatlerle saat sapmasını ε ≈ 1 - 7 ms aralığında sınırlar.
- Commit Wait değişmezi ile harici tutarlılık (linearizability).
- Paxos replikasyonu ve parçalar arası Two-Phase Commit (2PC).
- MVCC ile kilit almadan anlık okumalar.

=== 4. Gerçek Zamanlı Doküman Düzenleyici (Google Docs)
- Operational Transformation (OT) veya CRDT (Logoot/LSeq) çakışma çözümü.
- WebSocket uç ağ geçitleri ve sticky doküman yönlendirmesi.
- Append-only işlem günlüğü ve periyodik Spanner snapshot'ları.

=== 5. Google Drive / Bulut Depolama
- 4MB blok parçalama ve SHA-256 Content-Addressable Storage (CAS).
- Devam ettirilebilir yükleme (resumable upload) ile %40+ bant genişliği tasarrufu.
- Spanner metadata servisi ve Colossus erasure-coded blob depolama.

=== 6. YouTube Video Dağıtım & Streaming
- Transcoding DAG (5 saniyelik GOP parçaları ile paralel AV1, VP9, H.264 dönüştürme).
- DASH/HLS uyarlanabilir bit hızı ve çok katmanlı CDN uç önbellekleri.
- Pub/Sub + Apache Flink ile izlenme sayısı toplama akışı.

=== 7. Yüksek Verimli Metrik & İzleme Motoru (Google Monarch)
- Gorilla zaman serisi sıkıştırması: Delta-of-delta (1.37 bit) ve XOR float sıkıştırma.
- 2 saatlik bellek içi ring buffer ve sütun tabanlı disk arşivleme.

#pagebreak()

= Bölüm 5: Googleyness, Liderlik & L5/L6 Davranışsal Mülakatlar

== 5.1 Kıdemli Seviye Beklentileri
- *Belirsizlikte İlerleme*: Yetersiz tanımlanmış hedeflerden yol haritası ve RFC çıkarabilme.
- *Otorite Olmadan Liderlik*: Mühendislik ekiplerini emirle değil veriler ve benchmark testleriyle hizalama.
- *Disagree and Commit*: Veriye dayalı objektif tartışma ve alınan karara %100 adanmışlık.
- *Suçlamasız Kültür*: Üretim kesintilerini kişileri suçlamadan sistemsel otomasyon fırsatına dönüştürme.

== 5.2 STAR-L Formülü
- *Situation (%15)*: Yüksek riskli teknik karmaşıklık ve iş etkisi.
- *Task (%15)*: Temel belirsizlik ve kişisel sorumluluğunuz.
- *Action (%50)*: Somut teknik kararlar, takaslar ve uzlaşı adımları.
- *Result (%15)*: Ölçülebilir mühendislik metrikleri (gecikme, QPS, bulut maliyeti, uptime).
- *Learning (%5)*: Kazanılan mimari içgörü ve geriye dönük çıkarımlar.

#v(2em)
#align(center)[
  #text(fill: rgb("#1a73e8"), weight: "bold", size: 14pt)[
    Google Mülakat Hazırlığı Başarıyla Tamamlandı.
  ]
]
