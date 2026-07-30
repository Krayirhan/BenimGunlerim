## graphify

Bu projenin bilgi grafiği `graphify-out/` altındadır.

Kurallar:
- Mimari sorular için `graphify query "<soru>"` çalıştır
- İlişki araştırması için `graphify path "<A>" "<B>"` kullan
- `graphify-out/graph.json` varsa mimari sorularda önce graphify kullan
- Kod değişikliği sonrası `graphify update .` ile grafiği güncelle
- `/graphify` komutu geldiğinde graphify skill'ini devreye al

Ana agent kılavuzu: `CLAUDE.md`
Tasarım standardı: `DESIGN.md`
