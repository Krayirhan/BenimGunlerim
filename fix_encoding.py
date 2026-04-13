import os
import glob

replacements = {
    'Ã§': 'ç',
    'Ã‡': 'Ç',
    'ÄŸ': 'ğ',
    'Äž': 'Ğ',
    'Ä±': 'ı',
    'Ä°': 'İ',
    'Ã¶': 'ö',
    'Ã–': 'Ö',
    'ÅŸ': 'ş',
    'Åž': 'Ş',
    'Ã¼': 'ü',
    'Ãœ': 'Ü',
    'â€”': '—',
    'â€œ': '“',
    'â€': '”',
    'â€˜': '‘',
    'â€™': '’',
    'â„¢': '™',
    'âœ…': '✅',
    'â”€': '─',
    'ðŸ”„': '🔄',
    'âš¡': '⚡',
    'ðŸ”¥': '🔥',
    'ðŸ’¡': '💡'
}

files = glob.glob('D:\\BenimGunlerim\\app\\src\\main\\java\\com\\benimgunlerim\\ui\\**\\*.kt', recursive=True)
for file_path in files:
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()
        
        original = content
        for k, v in replacements.items():
            content = content.replace(k, v)
        
        if content != original:
            with open(file_path, 'w', encoding='utf-8') as f:
                f.write(content)
            print(f"Fixed encoding in: {file_path}")
    except Exception as e:
        print(f"Error processing {file_path}: {e}")
