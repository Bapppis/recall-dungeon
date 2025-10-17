#!/usr/bin/env python3
import json,os
root=r'C:\Users\Sasha\OneDrive\Desktop\Personal\Personal Projects\Recall Dungeon\recall-dungeon'
paths=[]
for dirpath,dirnames,filenames in os.walk(root):
    for fn in filenames:
        if fn.lower().endswith('.json'):
            # Only include files under data/properties
            pnorm=os.path.normpath(os.path.join(dirpath,fn)).lower()
            if os.path.sep + 'data' + os.path.sep + 'properties' in pnorm:
                paths.append(os.path.join(dirpath,fn))

from collections import defaultdict
m=defaultdict(list)
for p in paths:
    try:
        with open(p,'r',encoding='utf-8') as f:
            data=json.load(f)
            name=data.get('name')
            if name:
                key=' '.join(name.strip().lower().split())
                m[key].append(p)
            else:
                m['<no-name>:'+p].append(p)
    except Exception as e:
        m['<parse-error:'+p+'>'].append(str(e))

out=[]
for k in sorted(m):
    if len(m[k])>1:
        out.append((k,m[k]))

if not out:
    print('No duplicates found')
else:
    for k,files in out:
        print('NAME:',k)
        for pp in files:
            print('  -',pp)
        print()
