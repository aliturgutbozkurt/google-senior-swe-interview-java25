# Bölüm 5: Googleyness, Liderlik & L5/L6 Davranışsal Mülakatlar

## 1. Google Kıdemli Seviyede Neyi Ölçer?

Google'da **Senior Software Engineer (L5)** ve **Staff Software Engineer (L6)** adayları için özel bir **Googleyness & Leadership** oturumu uygulanır.

Bir aday kodlama ve sistem tasarımı turlarından tam puan alsa bile; kibirli davranırsa, belirsizlikte inisiyatif alamazsa veya teknik anlaşmazlıklarda yıkıcı bir tutum sergilerse İşe Alım Komitesi (Hiring Committee) adayı doğrudan **Reddeder (Reject)**.

```
                           Google L5/L6 Davranışsal Sütunları
                                        |
     +-------------------+-------------------+-------------------+
     |                   |                   |                   |
Belirsizlikte İlerleme   Otoritesiz Liderlik     Fikir Ayrılığı &    Psikolojik Güvenlik
 (Yol Haritası Çizme)     (RFC & Mutabakat)      Taahhüt (Veri)      (Suçlamasız Kültür)
```

---

## 2. 4 Temel Davranışsal Sinyal

### 2.1 Belirsizlikte İlerleme (Thriving in Ambiguity)
* **Junior (L3/L4)**: Net API dokümanı ve Jira görevleri olmadan çalışmakta zorlanır.
* **Senior (L5/L6)**: "Veritabanı maliyetlerimiz gelirden 3 kat hızlı artıyor, çöz" gibi muğlak bir hedef verildiğinde paydaşları toplar, hipotez kurar, metrikleri inceler, RFC yazar ve süreci otonom yönetir.

### 2.2 Otorite Olmadan Liderlik (Leadership Without Authority)
* Kıdemli mühendisler genelde doğrudan yönetici değildir; **etki, teknik güvenilirlik ve empati** ile liderlik ederler.
* Farklı üç ekibi yeni bir mimari standarda ikna etmek için güç kullanmazlar; teknik gerekçelendirme dokümanı (RFC), karşılaştırmalı benchmark testleri ve açık kaynak migration araçları sunarak uzlaşı sağlarlar.

### 2.3 Entelektüel Alçakgönüllülük & "Disagree and Commit"
* Teknik tartışmalarda kendi fikirlerine duygusal olarak bağlanmazlar.
* Karar toplantısında alternatifler gecikme, karmaşıklık ve bakım maliyetleri üzerinden objektif değerlendirilir. Ekip bir karara vardığında, kendi önerisi seçilmemiş olsa dahi alınan kararın başarısı için %100 adanmışlıkla çalışırlar.

### 2.4 Suçlamasız Kültür (Blameless Post-Mortem)
* Üretim çöktüğünde sorumlu bir insan aramak yerine, sistemdeki zafiyetlere odaklanılır.
* 5-Why kök neden analizi yapılarak bir insanın yapabileceği dikkatsizliğin sistemi çökertmesini engelleyecek otomasyonel emniyet supapları (Canary dağıtımı, Circuit breaker, Geri alma mekanizması) kurulur.

---

## 3. STAR-L Formülü (Kıdemli Mühendislik Versiyonu)

Mülakat yanıtları **STAR-L** çatısı ile yapılandırılmalıdır:

| Bileşen | Hedef Süre | Kıdemli Mühendis Stratejisi |
| :--- | :--- | :--- |
| **Situation (Durum)** | 1.0 dk | Yüksek riskli bağlam, sistem ölçeği (QPS, petabayt) ve iş riskini tanımlayın. |
| **Task (Görev)** | 0.5 dk | Temel belirsizliği ve sizin üstlendiğiniz kişisel teknik sorumluluğu belirtin. |
| **Action (Aksiyon)** | 2.5 dk | Aldığınız somut teknik kararları, tarttığınız takasları ve uzlaşı adımlarını "Biz" değil "Ben" diliyle aktarın. |
| **Result (Sonuç)** | 1.0 dk | Ölçülebilir mühendislik metrikleri (Gecikme %45 düştü, 1.2M$ bulut tasarrufu, sıfır kesinti). |
| **Learning (Öğrenim)** | 0.5 dk | Bu deneyimden edindiğiniz mimari içgörü ve geriye dönüp baksanız neyi farklı yapacağınız. |

---

## 4. Örnek Mülakat Senaryoları

### Senaryo 1: Yüksek Riskli Mimari Fikir Ayrılığı
**Soru**: *"Kritik bir tasarım kararında başka bir kıdemli mühendis veya mimar ile şiddetle ayrıştığınız bir anı anlatın. Bunu nasıl çözdünüz?"*

* **Situation**: Saniyede 100k olay işleyen streaming hattımızda mimarımız 5 saniyelik mikro-batch kullanan Spark Streaming önerirken, ben 200ms SLA hedefimiz için yerel event-driven mimarili Apache Flink önerdim.
* **Task**: Projeyi geciktirmeden ve ilişkilere zarar vermeden en doğru teknik kararda uzlaşmak.
* **Action**:
  1. Teorik tartışmalara girmeden p99 gecikmesi, checkpointing toparlanma süresi ve operasyonel karmaşıklığı içeren objektif bir karar matrisi oluşturdum.
  2. 48 saat içinde her iki motoru sentetik 3x tepe yük ve sırasız gelen verilerle stres testine tabi tutan kontrollü bir POC hazırladım.
  3. Benchmark sonuçları Flink'in dalgalı trafikte 85ms altında kaldığını, Spark'ın ise SLA'yı aştığını kanıtladı.
  4. Mimarın operasyonel kaygılarını gidermek adına kapsamlı Flink operasyon rehberleri ve hazır Prometheus alarmları yazdım.
* **Result**: Sistem Flink ile devreye alındı ve günlük 12 milyar işlem 85ms p99 gecikmeyle işlendi.
* **Learning**: Teknik tartışmaları kişiselleştirmek yerine somut benchmark verilerine dayandırmak egoları devre dışı bırakır ve süreci bilimsel bir keşfe dönüştürür.

---

### Senaryo 2: Kritik Üretim Kesintisi & Suçlamasız Kök Neden Analizi
**Soru**: *"Yaşadığınız veya çözümünü yönettiğiniz en büyük üretim kesintisini anlatın."*

* **Situation**: Kara Cuma indiriminde önbellek TTL sürelerinin senkron dolması nedeniyle veritabanı kümesinde thundering-herd oluştu ve ödeme alma başarısı %35 düştü.
* **Task**: Trafiği anında toparlamak ve hatanın tekrarlanmasını engellemek.
* **Action**:
  1. İlk 8 dakika içinde API gateway üzerinden acil yük budama (traffic shedding) açarak ve önceden ısıtılmış önbelleklerle okuma replikalarını devreye alarak sistemi kurtardım.
  2. "5-Why" metodolojisiyle suçlamasız post-mortem oturumunu yönettim: Önbellek sürelerine rastgele sapma (jitter) eklenmediği için 500.000 anahtarın aynı saniyede düştüğünü tespit ettik.
  3. Kütüphanelere jitter mekanizması ve erken olasılıksal yenileme (XFetch) ekleyen güncellemeyi yazdım.
* **Result**: %99.99 ödeme çalışma süresi sağlandı; sonraki satış günlerinde sıfır önbellek izdihamı yaşandı.
* **Learning**: Bir sistem bileşenlerinin mutlaka çökeceği varsayılarak tasarlanmalıdır; dayanıklılık şansa bırakılamaz.
