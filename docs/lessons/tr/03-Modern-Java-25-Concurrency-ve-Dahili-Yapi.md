# Bölüm 3: Modern Java 25 Eşzamanlılık, Bellek Modeli ve Dahili Yapı

## 1. Java 25 Sanal Thread (Virtual Thread) Mimarisi

Project Loom öncesinde Java thread'leri işletim sistemi (OS) çekirdek thread'lerine 1:1 oranında bağlıydı. 64-bit bir JVM sürecinde şu nedenlerle birkaç binden fazla thread açılamıyordu:
1. **Yığın Belleği (Stack)**: Her işletim sistemi thread'i 1MB sabit sanal bellek tüketir (`-Xss`).
2. **Bağlam Değiştirme (Context Switching)**: İşletim sistemi çekirdek geçişleri (Ring-3 $\to$ Ring-0) işlemciyi yorar ($\approx 1 - 2 \mu\text{s}$).

```
[Milyonlarca Sanal Thread (Virtual Threads)]
  VT 1   VT 2   VT 3   VT 4   VT 5 ... VT 1.000.000
    \      /      |      |      /
     \    /       |      |     /
    [ForkJoinPool Taşıyıcı Thread'ler (İşlemci Çekirdek Sayısı Kadar)]
       Carrier 1      Carrier 2      Carrier 3
           |              |              |
    [OS Çekirdek Thread 1] [OS Çekirdek Thread 2] [OS Çekirdek Thread 3]
```

### 1.1 Sanal Thread'ler Nasıl Çalışır?
* Sanal thread'ler tamamen JVM seviyesinde kullanıcı alanında (user-space) yönetilir.
* Çağrı yığınları Java Heap alanında **Continuation** nesneleri olarak saklanır ve birkaç yüz byte ile başlar.
* Bir sanal thread bloklayıcı I/O yaptığında (Socket okuması, veritabanı sorgusu, `park()`), alttaki continuation taşıyıcı thread'den (carrier) ayrılır (unmount). Taşıyıcı thread boşta beklemeden başka bir sanal thread'i çalıştırmaya devam eder.
* I/O tamamlandığında continuation uygun herhangi bir taşıyıcı thread'e yeniden bağlanır (mount).

### 1.2 Thread Pinning Riski
Bir sanal thread, I/O işlemi sırasında `synchronized` bloğu veya yerel bir JNI kodu içindeyse taşıyıcı thread'e kilitlenir (pinned). Bu durum diğer sanal thread'lerin çalışmasını bloke eder. Çözüm: `synchronized` yerine açık `ReentrantLock` kullanımıdır.

---

## 2. Java 25 Yapısal Eşzamanlılık (`StructuredTaskScope`)

Geleneksel `CompletableFuture` veya ham thread havuzları şu riskleri taşır:
* **Thread Sızıntısı (Thread Leak)**: İstemci zaman aşımına uğrasa bile arka planda öksüz kalan alt işlemler CPU ve I/O tüketmeye devam eder.
* **Kopuk İzlenebilirlik**: Üst işlem ile alt işlemler arasında hata izleme (stack trace) bağı kopar.

Java 25 ile gelen `StructuredTaskScope` **sözlüksel kapsam (lexical scope)** güvencesi verir:

```java
try (var scope = StructuredTaskScope.open(StructuredTaskScope.Joiner.awaitAllSuccessfulOrThrow())) {
    var profileTask = scope.fork(() -> fetchProfile(userId));
    var ordersTask  = scope.fork(() -> fetchOrders(userId));

    scope.join(); // Hepsi bitene veya biri hata verene kadar bekler
    return new UserData(profileTask.get(), ordersTask.get());
} // try bloğu bittiğinde tüm alt işlemlerin sonlandığı garanti edilir
```

### Spekülatif İstekler (Hedged Requests)
Dağıtık sistemlerde p99 gecikmesini düşürmek için 3 replikaya aynı anda yarış açılır:

```java
try (var scope = StructuredTaskScope.open(StructuredTaskScope.Joiner.<String>anySuccessfulResultOrThrow())) {
    scope.fork(() -> callReplica("us-east"));
    scope.fork(() -> callReplica("us-central"));
    scope.fork(() -> callReplica("us-west"));

    return (String) scope.join(); // İlk başarılı cevabı döner, diğerlerini anında iptal eder!
}
```

---

## 3. Scoped Values (`ScopedValue`) vs `ThreadLocal`

| Özellik | `ThreadLocal<T>` | `ScopedValue<T>` (Java 25) |
| :--- | :--- | :--- |
| **Değiştirilebilirlik** | Değiştirilebilir (`set()` her yerden çağrılabilir). | Kapsam içinde kesinlikle değişmez (immutable). |
| **Ömür** | Sınırsız (unutulursa bellek sızıntısı yapar). | Sadece tanımlandığı lambda veya scope içinde geçerlidir. |
| **Sanal Thread Uyumu** | Ağır (milyonlarca thread için map kopyalanır). | Hafif ($O(1)$ işaretçi ile alt thread'e miras kalır). |

---

## 4. Kilit Gerektirmeyen (Lock-Free) Veri Yapıları & JMM

### 4.1 Treiber Lock-Free Stack
* Tek bir `AtomicReference<Node<T>> head` üzerinde CAS döngüsü işletilir.
* `push`: `node.next = head.get()`, ardından `head.compareAndSet(oldHead, node)`.
* `Thread.onSpinWait()` komutu işlemcinin hiper-threading çekirdeğine ipucu vererek enerji ve çekişmeyi optimize eder.

### 4.2 Michael-Scott Lock-Free Queue
* Kuyruk başı (`head`) ve sonu (`tail`) için sahte sentinel düğüm kullanılır.
* Kuyruğa ekleme (enqueue) 2 aşamalı CAS ile gerçekleşir. Bir thread kuyruk sonunun geride kaldığını fark ederse, kendi eklemesinden önce kuyruğu ilerletmeye yardım eder (cooperative advancement).

### 4.3 Önbellek Satırı Sahte Paylaşımı (False Sharing)
* Modern CPU'lar veriyi 64-byte uzunluğundaki Cache Line bloklarında önbelleğe alır.
* İki bağımsız çekirdek aynı 64-byte bloğa düşen değişkenleri eşzamanlı güncellerse, L1/L2 önbellek sürekli geçersiz kılınır ve performans %90+ düşer.
* Çözüm: Değişkenler arasına dolgu (padding) eklemek veya `@Contended` kullanmaktır.
