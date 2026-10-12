# Bölüm 4: Google Ölçeğinde Dağıtık Sistem Tasarımı Mimarileri

## 1. Google Sistem Tasarımı Mülakat Çerçevesi

Google Senior (L5/L6) Sistem Tasarımı mülakatında (45 dakika) oturumu aday yönetir. Mülakatçı mimari sezginizi, veri depolama motoru seçimlerinizi ve hata senaryolarındaki takaslarınızı (trade-offs) puanlar:

```
00:00 - 05:00: Kapsam, Fonksiyonel / Fonksiyonel Olmayan Gereksinimler & API Sözleşmesi
  |
05:00 - 10:00: Kaba Taslak Kapasite Hesapları (QPS, IOPS, Depolama, Ağ Bant Genişliği)
  |
10:00 - 25:00: Üst Düzey Mimari, Bileşenler, Veri Akışı ve Veritabanı Şemaları
  |
25:00 - 40:00: Darboğazlar, Dağıtık Değişmezler, Ağ Bölünmeleri (Partitions) & Hata Modları
  |
40:00 - 45:00: İzlenebilirlik (SLO/SLA), Felaket Kurtarma, Özet & Mimari Takaslar
```

---

## 2. Dağıtık Sistemler Matematiği & Donanım Gecikmeleri

### 2.1 Her Google Mühendisinin Ezbere Bildiği Gecikme Rakamları
* **L1 Önbellek Erişimi**: $0.5 \text{ ns}$
* **L2 Önbellek Erişimi**: $7 \text{ ns}$
* **RAM (Ana Bellek) Erişimi**: $100 \text{ ns}$
* **RAM'den Sıralı 1 MB Okuma**: $3 \mu\text{s}$
* **NVMe SSD'den Sıralı 1 MB Okuma**: $250 \mu\text{s}$
* **Veri Merkezi İçi Ağ Gecikmesi (RTT)**: $500 \mu\text{s}$
* **Kıtalararası Ağ Gecikmesi (ABD Doğu - Batı)**: $40 \text{ ms}$
* **Okyanus Aşırı Gecikme (ABD - Avrupa)**: $100 \text{ ms}$

### 2.2 Pratik Kapasite Çarpanları
* Günde 100 Milyon İstek $\approx 1.200 \text{ QPS}$ (Zirve $\approx 2.500 \text{ QPS}$)
* Günde 1 Milyar İstek $\approx 12.000 \text{ QPS}$ (Zirve $\approx 30.000 \text{ QPS}$)
* 1 Byte $\times 10^9 \approx 1 \text{ GB}$ | 1 Byte $\times 10^{12} \approx 1 \text{ TB}$ | 1 Byte $\times 10^{15} \approx 1 \text{ PB}$

---

## 3. 7 Temel Google Ölçeğinde Sistem Mimarisi

### 3.1 Global Multi-Region Dağıtık Hız Sınırlayıcı (Rate Limiter)
* **Ölçek**: 10 Milyon QPS, 5 kıtada $< 2 \text{ ms}$ yanıt süresi.
* **Mimarisi**:
  1. **Uç Yönlendiriciler (GFE / Envoy)**: Bellek içi yerel token kovalarıyla sıfır ağ gecikmeli doğrulama.
  2. **Toplu Kota Kiralama (Batch Leasing)**: Her istekte merkezi veritabanına gidilmez; yerel proxy'ler bölgedeki Redis kümesinden örneğin 500'er tokenlık paketler kiralar.
  3. **Ağ Bölünmesinde Fail-Open Güvencesi**: Bölgeler arası WAN hattı koptuğunda proxy trafiği reddetmek yerine güvenli yerel sınırla hizmeti sürdürür.

---

### 3.2 Google-Scale Web Tarayıcısı (Web Crawler)
* **Ölçek**: Günde 5 Milyar sayfa tarama, host nezaketi (politeness) ve kopya içerik eliminasyonu.
* **Mimarisi**:
  1. **URL Frontier**: PageRank tabanlı öncelik kuyrukları ve domain bazlı gecikme kuyrukları (bir host'a saniyede en fazla 1 istek).
  2. **DNS Prefetching**: İşletim sistemi DNS darboğazını aşan bellek içi DNS çözümleyici.
  3. **İçerik Tekilleştirme**: 64-bit SimHash ve Ölçeklenebilir Bloom Filtreleri ile daha önce taranan sayfaların tekrar indirilmesi engellenir.
  4. **Depolama**: Ham HTML Colossus (GFS) üzerinde, metadata ise Google Bigtable'da saklanır.

---

### 3.3 Google Spanner: Dağıtık İlişkisel Veritabanı
* **Ölçek**: Global çok bölgeli ACID işlemleri ve harici tutarlılık (External Consistency / Linearizability).
* **Mimarisi**:
  1. **TrueTime API**: GPS ve atomik saatlerle senkronize saat sapmasını $[\epsilon \approx 1 - 7 \text{ ms}]$ aralığında sınırlar.
  2. **Commit Wait Değişmezi**: Lider düğüm $t_{\text{commit}}$ zaman damgasını verdikten sonra en az $\epsilon$ kadar uyur. Böylece sonraki tüm işlemler kesinlikle daha büyük bir zaman damgası alır.
  3. **Paxos Grupları**: Her veri parçası (directory) 3-5 bölgede Paxos ile kopyalanır.
  4. **2PC over Paxos**: Parçalar arası dağıtık işlemlerde Paxos liderleri Two-Phase Commit ile koordine edilir.
  5. **MVCC**: Belirli bir $t$ anındaki okumalar kilit almadan yerel replikadan gerçekleştirilir.

---

### 3.4 Gerçek Zamanlı İşbirlikçi Doküman Düzenleyici (Google Docs)
* **Ölçek**: Aynı dokümanda 100+ eşzamanlı yazar, $< 50 \text{ ms}$ tuş gecikmesi.
* **Mimarisi**:
  1. **Çakışma Çözümü**: Operational Transformation (OT) veya CRDT (Logoot / LSeq). CRDT'de karakterler kesirli global indeksler alır; merkezi sunucuya ihtiyaç duymadan birleşebilir.
  2. **WebSocket Gateway**: Doküman oturumlarına sticky yönlendirme yapan uç proxy'ler.
  3. **İşlem Günlüğü & Snapshot**: Tüm operasyonlar append-only log olarak Spanner'a yazılır; periyodik snapshot alınır.

---

### 3.5 Google Drive / Global Bulut Depolama
* **Ölçek**: 1 Milyar aktif kullanıcı, petabaytlarca dosya yüklemesi, blok seviyesinde tekilleştirme.
* **Mimarisi**:
  1. **4MB Bloklama & CAS**: Dosyalar 4MB bloklara ayrılır ve SHA-256 hash'leri alınır.
  2. **Devam Ettirilebilir Yükleme (Resumable Upload)**: İstemci yükleme yapmadan önce hash'i sorar; blok zaten varsa yükleme atlanır (%40+ bant genişliği tasarrufu).
  3. **Depolama**: Bloklar Colossus üzerinde Reed-Solomon ($8+4$) hata düzeltme kodlarıyla disk arızalarına karşı korunur.

---

### 3.6 YouTube Video Dağıtım & Streaming
* **Ölçek**: Dakikada 500 saat video yüklemesi, 2 Milyar günlük izleyici.
* **Mimarisi**:
  1. **Transcoding DAG**: Yüklenen video 5 saniyelik GOP (Group of Pictures) parçalarına bölünür ve paralel işçi havuzunda farklı çözünürlüklere (1080p, 4K, AV1, VP9) dönüştürülür.
  2. **Uyarlanabilir Bit Hızı (DASH / HLS)**: İstemci bant genişliğini ölçerek video parçalarını HTTP byte-range istekleriyle indirir.
  3. **Çok Katmanlı CDN**: En popüler %10'luk video içeriği uç noktalarda (Edge PoP) tutularak trafiğin %80'i karşılanır.

---

### 3.7 Yüksek Verimli Metrik & İzleme Motoru (Google Monarch)
* **Ölçek**: Saniyede on milyonlarca metrik zaman serisi toplama, 1 saniyenin altında kural değerlendirmesi.
* **Mimarisi**:
  1. **Gorilla Zaman Serisi Sıkıştırması**: Zaman damgalarında delta-of-delta kodlaması ($1.37$ bit/nokta) ve değerlerde XOR kayan nokta sıkıştırması.
  2. **İki Katmanlı Bellek**: Son 2 saatin verisi anlık alarmlar için RAM dairesel tamponunda, geçmiş veri ise sütun tabanlı diskte arşivlenir.
